import static org.assertj.core.api.Assertions.assertThat;

import io.github.algomaster99.Terminator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.junit.jupiter.api.Test;

public class AgentTest {
    @Test
    void shouldDisallowLoadingCustomJDKClass() throws MavenInvocationException, IOException, InterruptedException {
        // contract: watchdog-agent should detect if the class masquerading as an internal class

        Path project = Paths.get("src/test/resources/load_jdk_class");

        recursiveDeleteOnShutdownHook(project.resolve("target"));

        InvocationRequest request = new DefaultInvocationRequest();
        File pomFile = project.resolve("pom.xml").toFile();
        request.setPomFile(pomFile);
        request.setGoals(List.of("clean", "package"));

        Invoker invoker = new DefaultInvoker();
        InvocationResult result = invoker.execute(request);

        assertThat(result.getExitCode()).isEqualTo(0);

        String fingerprintFile =
                project.resolve("target").resolve("classfile.sha256.jsonl").toString();
        deleteContentsOfFile(fingerprintFile);

        String agentArgs = "fingerprints=" + fingerprintFile;
        String[] cmd = {
            "java",
            "-javaagent:" + getAgentPath(agentArgs),
            "-jar",
            project.resolve("target")
                    .resolve("jdk_class-1.0-SNAPSHOT-jar-with-dependencies.jar")
                    .toString()
        };
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);

        Process p = pb.start();
        int exitCode = p.waitFor();

        assertThat(exitCode).isEqualTo(1);
    }

    private static void deleteContentsOfFile(String file) throws InterruptedException, IOException {
        String[] deleteFile = {"rm", "-f", file};
        Runtime.getRuntime().exec(deleteFile).waitFor();

        String[] createFile = {"touch", file};
        Runtime.getRuntime().exec(createFile).waitFor();
    }

    private static String getAgentPath(String agentArgs) throws IOException {
        String tempDir = System.getProperty("java.io.tmpdir");
        Path traceCollector = Path.of(tempDir, "watchdog-agent.jar");

        try (InputStream traceCollectorStream = Terminator.class.getResourceAsStream("/watchdog-agent.jar")) {
            Files.copy(traceCollectorStream, traceCollector, StandardCopyOption.REPLACE_EXISTING);
        }

        return traceCollector.toAbsolutePath() + "=" + agentArgs;
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
