package org.example.it;

import static com.soebes.itf.extension.assertj.MavenITAssertions.assertThat;

import com.soebes.itf.jupiter.extension.MavenGoal;
import com.soebes.itf.jupiter.extension.MavenJupiterExtension;
import com.soebes.itf.jupiter.extension.MavenOption;
import com.soebes.itf.jupiter.extension.MavenTest;
import com.soebes.itf.jupiter.maven.MavenExecutionResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

@MavenJupiterExtension
class GenerateMojoIT {

    @Nested
    class Algorithm {
        private static final int CLASSFILES_LOADED = 5992;

        @DisplayName("SHA256 - the default algorithm")
        @MavenGoal("${project.groupId}:${project.artifactId}:${project.version}:generate")
        @MavenTest
        void sha256(MavenExecutionResult result) {
            assertThat(result).isSuccessful();

            Path fingerprintFile =
                    getFingerprint(result.getMavenProjectResult().getTargetProjectDirectory(), "classfile.sha256");

            assertThat(fingerprintFile).isRegularFile().content().hasLineCount(CLASSFILES_LOADED);
        }

        @DisplayName("SHA1")
        @MavenGoal("${project.groupId}:${project.artifactId}:${project.version}:generate")
        @MavenOption("-Dalgorithm=SHA1")
        @MavenTest
        void sha1(MavenExecutionResult result) {
            assertThat(result).isSuccessful();

            Path fingerprintFile =
                    getFingerprint(result.getMavenProjectResult().getTargetProjectDirectory(), "classfile.sha1");

            assertThat(fingerprintFile).isRegularFile().content().hasLineCount(CLASSFILES_LOADED);
        }

        @DisplayName("MD5")
        @MavenGoal("${project.groupId}:${project.artifactId}:${project.version}:generate")
        @MavenOption("-Dalgorithm=MD5")
        @MavenTest
        void md5(MavenExecutionResult result) {
            assertThat(result).isSuccessful();

            Path fingerprintFile =
                    getFingerprint(result.getMavenProjectResult().getTargetProjectDirectory(), "classfile.md5");

            assertThat(fingerprintFile).isRegularFile().content().hasLineCount(CLASSFILES_LOADED);
        }
    }

    @MavenGoal("${project.groupId}:${project.artifactId}:${project.version}:generate")
    @MavenTest
    void guava(MavenExecutionResult result) throws IOException {
        assertThat(result).isSuccessful();

        Path projectDirectory = result.getMavenProjectResult().getTargetProjectDirectory();

        Path actualFingerprint = getFingerprint(projectDirectory, "classfile.sha256");

        Path expectedFingerprint =
                Path.of(projectDirectory.toString(), "src", "test", "resources", "expected-classfile.sha256");
        String expectedContent = Files.readString(expectedFingerprint);

        assertThat(actualFingerprint).isRegularFile().hasContent(expectedContent);
    }

    @DisplayName("Classfile fingerprint with native dependency")
    @MavenGoal("${project.groupId}:${project.artifactId}:${project.version}:generate")
    @MavenTest
    void native_dependency(MavenExecutionResult result) throws IOException {
        assertThat(result).isSuccessful();

        String osName = System.getProperty("os.name");

        Path projectDirectory = result.getMavenProjectResult().getTargetProjectDirectory();

        Path actualFingerprint = getFingerprint(projectDirectory, "classfile.sha256");

        Path expectedFingerprint;

        if (osName.contains("Mac")) {
            expectedFingerprint =
                    Path.of(projectDirectory.toString(), "src", "test", "resources", "expected-classfile.MacOS.sha256");
        } else if (osName.contains("Linux")) {
            expectedFingerprint =
                    Path.of(projectDirectory.toString(), "src", "test", "resources", "expected-classfile.Linux.sha256");
        } else if (osName.contains("Windows")) {
            expectedFingerprint = Path.of(
                    projectDirectory.toString(), "src", "test", "resources", "expected-classfile.Windows.sha256");
        } else {
            throw new IllegalStateException("Unsupported OS: " + osName);
        }

        String expectedContent = Files.readString(expectedFingerprint);

        assertThat(actualFingerprint).isRegularFile().hasContent(expectedContent);
    }

    @DisplayName("Different fingerprint should be generated for sub-modules")
    @MavenGoal("${project.groupId}:${project.artifactId}:${project.version}:generate")
    @MavenTest
    void multi_module(MavenExecutionResult result) throws IOException {
        assertThat(result).isSuccessful();

        Path rootModule = result.getMavenProjectResult().getTargetProjectDirectory();

        Path a = Path.of(rootModule.toString(), "a");
        Path b = Path.of(rootModule.toString(), "b");

        Path rootFingerPrint = getFingerprint(rootModule, "classfile.sha256");
        assertThat(rootFingerPrint).isRegularFile().isEmptyFile();

        Path aFingerPrint = getFingerprint(a, "classfile.sha256");
        assertThat(aFingerPrint).isRegularFile().isNotEmptyFile();

        Path bFingerPrint = getFingerprint(b, "classfile.sha256");
        assertThat(bFingerPrint).isRegularFile().isNotEmptyFile();
    }

    private static Path getFingerprint(Path projectDirectory, String classfileFingerprintName) {
        return Path.of(projectDirectory.toString(), "target", classfileFingerprintName);
    }
}
