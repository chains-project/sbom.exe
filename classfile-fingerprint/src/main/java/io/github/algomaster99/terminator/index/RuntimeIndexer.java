package io.github.algomaster99.terminator.index;

import io.github.algomaster99.terminator.commons.fingerprint.provenance.Provenance;
import io.github.algomaster99.terminator.preprocess.PomTransformer;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
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

    @Override
    Map<String, Set<Provenance>> createOrMergeProvenances(Map<String, Set<Provenance>> referenceProvenance) {
        return null;
    }

    @Override
    public Integer call() throws Exception {
        Path pathToTempProject = createCopyOfProject(project);
        if (cleanup) {
            recursiveDeleteOnShutdownHook(pathToTempProject.getParent());
        }
        transformPomsRecursively(pathToTempProject);
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

    private static void transformPomsRecursively(Path projectRoot) throws IOException {
        Files.walk(projectRoot)
                .filter(path -> path.getFileName().toString().equals("pom.xml")
                        && !path.toString().contains("/resources/"))
                .forEach(path -> {
                    try {
                        PomTransformer transformer = new PomTransformer(path);
                        transformer.transform();
                        transformer.writeTransformedPomInPlace();

                    } catch (XmlPullParserException | IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
