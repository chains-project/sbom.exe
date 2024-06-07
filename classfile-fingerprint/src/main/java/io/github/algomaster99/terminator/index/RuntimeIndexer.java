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
import java.util.HashSet;
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
            description = "Delete the temporary project after the process",
            required = false)
    private boolean cleanup = false;

    @CommandLine.Option(
            names = {"-mj", "--executable-jar-module"},
            description = "The module that generates the executable jar",
            required = true)
    private String executableJarModule;

    @Override
    public Integer call() throws Exception {
        System.out.println("--------------------");
        System.out.println("Indexing all runtime classes ...");
        Path pathToTempProject = createCopyOfProject(project);
        MavenModule rootProject = MavenModule.createMavenModuleGraph(pathToTempProject);
        MavenModule executableJar = rootProject.findSubmodule(executableJarModule);
        if (executableJar == null) {
            throw new RuntimeException("The module " + executableJarModule + " is not found in the project");
        }
        Set<MavenModule> requiredModules = executableJar.getSubmodulesThatAreDependencies();
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
                if (!output.toFile().exists()) {
                    output.toFile().createNewFile();
                }
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
        request.addArg("-fae");
        request.setGoals(List.of("clean", "package"));
        request.setBatchMode(true);
        request.setQuiet(true);
        Invoker invoker = new DefaultInvoker();

        System.out.println("Ignore the test output below.");

        invoker.execute(request);

        System.out.println("Ignore the test output above.");

        final Map<String, Set<ClassFileAttributes>> referenceProvenance = new HashMap<>();

        for (Path index : candidateIndexForMerge) {
            Map<String, Set<ClassFileAttributes>> generatedReferenceProvenance =
                    ParsingHelper.deserializeFingerprints(index);
            if (generatedReferenceProvenance.isEmpty()) {
                System.out.println("There were no tests for: " + index);
                continue;
            }
            Map<String, Set<ClassFileAttributes>> updatedReferenceProvenance =
                    createOrMergeProvenances(generatedReferenceProvenance);
            updatedReferenceProvenance.forEach((k, v) -> {
                referenceProvenance.computeIfAbsent(k, className -> new HashSet<>(v));
                referenceProvenance.computeIfPresent(k, (className, existing) -> {
                    existing.addAll(v);
                    return existing;
                });
            });
        }

        if (indexFile.input != null) {
            ParsingHelper.serialiseFingerprints(referenceProvenance, indexFile.input.toPath());
            System.out.println(
                    String.format("Classes in %s: %d.", indexFile.input.getName(), referenceProvenance.size()));
            System.out.println("--------------------");
        } else if (indexFile.output != null) {
            ParsingHelper.serialiseFingerprints(referenceProvenance, indexFile.output.toPath());
            System.out.println(
                    String.format("Classes in %s: %d.", indexFile.output.getName(), referenceProvenance.size()));
            System.out.println("--------------------");
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
                currentReferenceProvenance.computeIfAbsent(k, className -> new HashSet<>(v));
                currentReferenceProvenance.computeIfPresent(k, (className, existing) -> {
                    existing.addAll(v);
                    return existing;
                });
            });
            return currentReferenceProvenance;
        }
        if (indexFile.output != null) {
            Map<String, Set<ClassFileAttributes>> currentReferenceProvenance = new HashMap<>();
            referenceProvenance.forEach((k, v) -> {
                currentReferenceProvenance.computeIfAbsent(k, className -> new HashSet<>(v));
                currentReferenceProvenance.computeIfPresent(k, (className, existing) -> {
                    existing.addAll(v);
                    return existing;
                });
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
            Path pathToGitDirectoryOrFile = destinationDirectory.toPath().resolve(".git");
            if (pathToGitDirectoryOrFile.toFile().isDirectory()) {
                FileUtils.deleteDirectory(pathToGitDirectoryOrFile.toFile());
            }
            if (pathToGitDirectoryOrFile.toFile().isFile()) {
                Files.delete(pathToGitDirectoryOrFile);
            }
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
