package io.github.algomaster99.terminator.index;

import io.github.algomaster99.terminator.commons.fingerprint.provenance.Provenance;
import io.github.algomaster99.terminator.commons.maven.MavenModule;
import io.github.algomaster99.terminator.commons.maven.MavenModuleDependencyGraph;
import io.github.algomaster99.terminator.commons.options.RuntimeClassInterceptorOptions;
import io.github.algomaster99.terminator.preprocess.PomTransformer;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import org.apache.commons.io.FileUtils;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import picocli.CommandLine;

@CommandLine.Command(
        name = "runtime",
        mixinStandardHelpOptions = true,
        description = "Create an index of classes exercised during test")
public class RuntimeIndexer extends BaseIndexer implements Callable<Integer> {

    @CommandLine.Option(
            names = {"-p", "--project"},
            required = true,
            description = "Project for which the classes needs to be recorded")
    private Path project;

    @CommandLine.Option(
            names = {"--cleanup"},
            description = "The selected methods",
            required = false)
    private boolean cleanup = false;

    @CommandLine.Option(
            names = {"-mj", "--executable-jar-module"},
            description = "The module that generates the executable jar",
            required = true)
    private String executableJarModule;

    @Override
    Map<String, Set<Provenance>> createOrMergeProvenances(Map<String, Set<Provenance>> referenceProvenance) {
        return null;
    }

    @Override
    public Integer call() throws Exception {
        Path pathToTempProject = createCopyOfProject(project);
        MavenModule rootProject = MavenModuleDependencyGraph.createMavenModuleGraph(pathToTempProject);
        MavenModule executableJar = rootProject.findSubmodule(executableJarModule);
        if (executableJar == null) {
            throw new RuntimeException("The module " + executableJarModule + " is not found in the project");
        }
        List<MavenModule> requiredModules = executableJar.getSubmodulesThatAreDependencies();
        // we want to instrument the executable jar module as well
        requiredModules.add(executableJar);
        if (cleanup) {
            recursiveDeleteOnShutdownHook(pathToTempProject.getParent());
        }
        for (MavenModule module : requiredModules) {
            RuntimeClassInterceptorOptions options = new RuntimeClassInterceptorOptions();
            if (indexFile.output != null) {
                String suffix = module.getSelf().getArtifactId();
                options.setOutput(Path.of(indexFile.output.toString() + "_" + suffix + ".jsonl"));
            }
            PomTransformer transformer =
                    new PomTransformer(module.getFileSystemPath().resolve("pom.xml"), options);
            transformer.transform();
            transformer.writeTransformedPomInPlace();
        }
        InvocationRequest request = new DefaultInvocationRequest();
        File pomFile = pathToTempProject.resolve("pom.xml").toFile();
        request.setPomFile(pomFile);
        request.setGoals(List.of("clean", "package"));
        Invoker invoker = new DefaultInvoker();
        invoker.execute(request);
        return 0;
    }

    private static Path createCopyOfProject(Path project) {
        try {
            Path tempDirectory = Files.createTempDirectory(project.getFileName().toString());
            File sourceDirectory = project.toFile();
            File destinationDirectory =
                    tempDirectory.resolve(project.toFile().getName()).toFile();
            FileUtils.copyDirectory(sourceDirectory, destinationDirectory);
            FileUtils.deleteDirectory(
                    destinationDirectory.toPath().resolve(".git").toFile());
            return destinationDirectory.toPath();
        } catch (IOException e) {
            throw new RuntimeException("Copy of the project could not be created: " + e);
        }
    }

    private static void recursiveDeleteOnShutdownHook(final Path path) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Files.walkFileTree(path, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, @SuppressWarnings("unused") BasicFileAttributes attrs)
                            throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                        if (e == null) {
                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                        }
                        // directory iteration failed
                        throw e;
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete " + path, e);
            }
        }));
    }
}
