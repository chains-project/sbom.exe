package io.github.algomaster99.terminator.index;

import io.github.algomaster99.terminator.commons.fingerprint.ParsingHelper;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileAttributes;
import io.github.algomaster99.terminator.commons.maven.MavenModule;
import io.github.algomaster99.terminator.commons.options.RuntimeClassInterceptorOptions;
import io.github.algomaster99.terminator.preprocess.PomTransformer;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
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
    public Integer call() throws Exception {
        Path pathToTempProject = createCopyOfProject(project);
        MavenModule rootProject = MavenModule.createMavenModuleGraph(pathToTempProject);
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
        List<Path> candidateIndexForMerge = new ArrayList<>();
        for (MavenModule module : requiredModules) {
            RuntimeClassInterceptorOptions options = new RuntimeClassInterceptorOptions();

            if (indexFile.output != null) {
                String suffix = module.getSelf().getArtifactId();
                Path output = Path.of(indexFile.output.toString() + "_" + suffix + ".jsonl");
                options.setOutput(output);
                candidateIndexForMerge.add(output);
            }
            if (indexFile.input != null) {
                String prefix = module.getSelf().getArtifactId();
                Path temporaryFile = Files.createTempFile(prefix, ".jsonl");
                options.setOutput(temporaryFile);
                candidateIndexForMerge.add(temporaryFile);
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

        final Map<String, Set<ClassFileAttributes>> referenceProvenance = new HashMap<>();

        for (Path index : candidateIndexForMerge) {
            Map<String, Set<ClassFileAttributes>> generatedReferenceProvenance =
                    ParsingHelper.deserializeFingerprints(index);
            Map<String, Set<ClassFileAttributes>> updatedReferenceProvenance =
                    createOrMergeProvenances(generatedReferenceProvenance);
            referenceProvenance.putAll(updatedReferenceProvenance);
        }

        if (indexFile.input != null) {
            ParsingHelper.serialiseFingerprints(referenceProvenance, indexFile.input.toPath());
        } else if (indexFile.output != null) {
            ParsingHelper.serialiseFingerprints(referenceProvenance, indexFile.output.toPath());
        }

        return 0;
    }

    @Override
    Map<String, Set<ClassFileAttributes>> createOrMergeProvenances(
            Map<String, Set<ClassFileAttributes>> referenceProvenance) {
        if (indexFile.input != null) {
            Map<String, Set<ClassFileAttributes>> currentReferenceProvenance =
                    ParsingHelper.deserializeFingerprints(indexFile.input.toPath());

            referenceProvenance.forEach((k, v) -> {
                if (currentReferenceProvenance.containsKey(k)) {
                    currentReferenceProvenance.get(k).addAll(v);
                } else {
                    currentReferenceProvenance.put(k, v);
                }
            });
            return currentReferenceProvenance;
        }
        if (indexFile.output != null) {
            Map<String, Set<ClassFileAttributes>> currentReferenceProvenance;
            if (indexFile.output.toPath().toFile().exists()) {
                currentReferenceProvenance = ParsingHelper.deserializeFingerprints(indexFile.output.toPath());
            } else {
                currentReferenceProvenance = new HashMap<>();
            }
            referenceProvenance.forEach((k, v) -> {
                if (currentReferenceProvenance.containsKey(k)) {
                    currentReferenceProvenance.get(k).addAll(v);
                } else {
                    currentReferenceProvenance.put(k, v);
                }
            });
            return currentReferenceProvenance;
        }
        throw new IllegalArgumentException("Either --input or --output must be specified");
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