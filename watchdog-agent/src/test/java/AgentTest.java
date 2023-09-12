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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

public class AgentTest {
    @Disabled("Should be worked upon after we know what java version is used by the application")
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

    // level 1: fat jar
    @Nested
    class Level1_FatJar {

        @Nested
        class PDFBox {
            private final Path project = Paths.get("src/test/resources/pdfbox-3.0.0");

            @Test
            void pdfbox_3_0_0_depscan_4_2_2(@TempDir Path tempDir) throws IOException, InterruptedException {
                // contract: pdfbox 3.0.0 should fail to execute as the SBOM missed dependency. For example,
                // picocli/CommandLine$ParameterException
                Path output = tempDir.resolve("output.txt");
                assertThat(runPDFBoxWithSbom(project.resolve("depscan_pdfbox-app.json"), output))
                        .isEqualTo(1);
            }

            @Test
            void pdfbox_3_0_0_buildInfoGo_1_9_9(@TempDir Path tempDir) throws IOException, InterruptedException {
                // contract: pdfbox 3.0.0 should execute as the SBOM has every dependency
                Path output = tempDir.resolve("output.txt");
                assertThat(runPDFBoxWithSbom(project.resolve("build-info-go.json"), output))
                        .isEqualTo(0);
            }

            private int runPDFBoxWithSbom(Path sbom, Path output) throws IOException, InterruptedException {
                Path pdfboxExecutable = project.resolve("pdfbox-app-3.0.0.jar");
                Path workload = project.resolve("2303.11102.pdf").toAbsolutePath();
                String agentArgs = "sbom=" + sbom;
                String[] cmd = {
                    "java",
                    "-javaagent:" + getAgentPath(agentArgs),
                    "-jar",
                    pdfboxExecutable.toString(),
                    "export:text",
                    "--input",
                    workload.toString(),
                    "--output",
                    output.toString()
                };
                ProcessBuilder pb = new ProcessBuilder(cmd);
                pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
                pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                pb.redirectError(ProcessBuilder.Redirect.INHERIT);

                Process p = pb.start();
                return p.waitFor();
            }
        }

        @DisabledOnOs(value = OS.WINDOWS, disabledReason = "It is extremely slow on Windows.")
        @Nested
        // We use TTorrent 2.0 based on release page however its actual version in POM is 1.2
        class TTorrent {
            private final Path project = Paths.get("src/test/resources/ttorent-2.0");

            @Test
            void ttorrent_2_0_buildInfoGo_1_9_9(@TempDir Path tempDir) throws IOException, InterruptedException {
                // contract: ttorrent 1.2 fails because its Jar does not exist on maven central.
                // Not sure if it is somewhere else.
                assertThat(runTTorrentWithSbom(project.resolve("build-info-go.json"), tempDir))
                        .isEqualTo(1);
            }

            private int runTTorrentWithSbom(Path sbom, Path output) throws IOException, InterruptedException {
                Path tTorrentExecutable = project.resolve("ttorrent-cli-1.2-shaded.jar");
                Path torrent = project.resolve("test.torrent").toAbsolutePath();
                String agentArgs = "sbom=" + sbom;
                String[] cmd = {
                    "java",
                    "-javaagent:" + getAgentPath(agentArgs),
                    "-jar",
                    tTorrentExecutable.toString(),
                    torrent.toString(),
                    "--output",
                    output.toAbsolutePath().toString(),
                };
                ProcessBuilder pb = new ProcessBuilder(cmd);
                pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
                pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                pb.redirectError(ProcessBuilder.Redirect.INHERIT);

                Process p = pb.start();
                return p.waitFor();
            }
        }
    }

    @Nested
    class Level2_CompositeJar {
        private final Path project = Path.of("src/test/resources/pdfbox-3.0.0");

        @Test
        void pdfbox_3_0_0_cyclonedx_2_7_4(@TempDir Path dir) throws IOException, InterruptedException {
            // contract: pdfbox-tools 3.0.0 should not execute as the SBOM has no dependencies
            Path output = dir.resolve("output.txt");
            assertThat(runPDFBoxWithSbom(project.resolve("bom.json"), output)).isEqualTo(1);
        }

        @Test
        void pdfbox_3_0_0_depscan_4_2_2(@TempDir Path dir) throws IOException, InterruptedException {
            // contract: pdfbox-tools 3.0.0 should not execute as the SBOM has no root component
            Path output = dir.resolve("output.txt");
            assertThat(runPDFBoxWithSbom(project.resolve("sbom-universal.json"), output))
                    .isEqualTo(1);
        }

        private int runPDFBoxWithSbom(Path sbom, Path output) throws IOException, InterruptedException {
            Path appWhichContainsExecutable = project.resolve("pdfbox-tools-3.0.0.jar");
            String mainClass = "org.apache.pdfbox.tools.PDFBox";
            Path workload = project.resolve("2303.11102.pdf").toAbsolutePath();

            Path dependency = project.resolve("dependency");
            String agentArgs = "sbom=" + sbom;
            String[] cmd = {
                "java",
                "-javaagent:" + getAgentPath(agentArgs),
                "-cp",
                appWhichContainsExecutable + ":" + dependency + "/*",
                // convert PDFs to text file
                mainClass,
                "export:text",
                "--input",
                workload.toString(),
                "--output",
                output.toString()
            };
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);

            Process p = pb.start();
            return p.waitFor();
        }
    }

    @Nested
    class Level3_ApplicationLevelClassLoading {
        private final Path project = Path.of("src/test/resources/sorald-0.8.5");

        @Test
        void sorald_0_8_5_depscan_4_2_2_fromCompositeJar() throws IOException, InterruptedException {
            // contract: sorald 0.8.5 should not execute as the SBOM + external jars has every dependency, except there
            // is no root component in the SBOM so we don't have classes of sorald-0.8.5 itself.

            Path sbom = project.resolve("sbom-universal.json");
            Path externalJars = project.resolve("external-jars.json").toAbsolutePath();
            Path appWhichContainsExecutable = project.resolve("sorald-0.8.5.jar");
            String mainClass = "sorald.Main";
            Path dependency = project.resolve("dependency");
            Path fileWithWarning = project.resolve("App.java").toAbsolutePath();

            String agentArgs = "sbom=" + sbom + ",externalJars=" + externalJars;
            String[] cmd = {
                "java",
                "-javaagent:" + getAgentPath(agentArgs),
                "-cp",
                appWhichContainsExecutable + ":" + dependency + "/*",
                mainClass,
                "mine",
                "--source",
                fileWithWarning.toString()
            };
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);

            Process p = pb.start();
            int exitCode = p.waitFor();
            assertThat(exitCode).isEqualTo(1);
        }

        @Test
        void sorald_0_8_5_cyclonedx_2_7_4_fromCompositeJar() throws IOException, InterruptedException {
            // contract: sorald 0.8.5 should fail as the SBOM misses all the dependencies.
            // For example, it cannot find picocli.CommandLine.

            Path sbom = project.resolve("bom.json");
            Path externalJars = project.resolve("external-jars.json").toAbsolutePath();
            Path appWhichContainsExecutable = project.resolve("sorald-0.8.5.jar");
            String mainClass = "sorald.Main";
            Path fileWithWarning = project.resolve("App.java").toAbsolutePath();

            String agentArgs = "sbom=" + sbom + ",externalJars=" + externalJars;
            String[] cmd = {
                "java",
                "-javaagent:" + getAgentPath(agentArgs),
                "-cp",
                // we exclude dependencies as CycloneDX SBOM misses all the dependencies
                appWhichContainsExecutable.toString(),
                mainClass,
                "mine",
                "--source",
                fileWithWarning.toString()
            };
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);

            Process p = pb.start();
            int exitCode = p.waitFor();
            assertThat(exitCode).isEqualTo(1);
        }

        @Test
        void sorald_0_8_5_cyclonedx_2_7_4_fromFatJar() throws IOException, InterruptedException {
            // contract: sorald 0.8.5 should execute as the fat jar + external jars has every dependency.
            Path project = Paths.get("src/test/resources/sorald-0.8.5");

            Path sbom = project.resolve("bom.json");
            Path externalJars = project.resolve("external-jars.json").toAbsolutePath();
            Path soraldExecutable = project.resolve("sorald-0.8.5-jar-with-dependencies.jar");
            Path fileWithWarning = project.resolve("App.java").toAbsolutePath();

            String agentArgs = "sbom=" + sbom + ",externalJars=" + externalJars;
            String[] cmd = {
                "java",
                "-javaagent:" + getAgentPath(agentArgs),
                "-jar",
                soraldExecutable.toString(),
                "mine",
                "--source",
                fileWithWarning.toString()
            };
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);

            Process p = pb.start();
            int exitCode = p.waitFor();
            assertThat(exitCode).isEqualTo(0);
        }
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
                        Files.deleteIfExists(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                        if (e == null) {
                            Files.deleteIfExists(dir);
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
