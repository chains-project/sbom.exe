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

    private static Path getFingerprint(Path projectDirectory, String classfileFingerprintName) {
        return Path.of(projectDirectory.toString(), "target", classfileFingerprintName);
    }
}
