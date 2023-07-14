package org.example.it;

import static com.soebes.itf.extension.assertj.MavenITAssertions.assertThat;

import com.soebes.itf.jupiter.extension.MavenGoal;
import com.soebes.itf.jupiter.extension.MavenJupiterExtension;
import com.soebes.itf.jupiter.extension.MavenTest;
import com.soebes.itf.jupiter.maven.MavenExecutionResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@MavenJupiterExtension
class GenerateMojoIT {
    @MavenGoal("${project.groupId}:${project.artifactId}:${project.version}:generate")
    @MavenTest
    void hello_world(MavenExecutionResult result) {
        assertThat(result).isSuccessful();
    }

    @MavenGoal("${project.groupId}:${project.artifactId}:${project.version}:generate")
    @MavenTest
    void guava(MavenExecutionResult result) throws IOException {
        assertThat(result).isSuccessful();

        Path projectDirectory = result.getMavenProjectResult().getTargetProjectDirectory();

        Path targetDirectory = Path.of(projectDirectory.toString(), "target");
        Path actualFingerprint = Path.of(targetDirectory.toString(), "classfile.sha-256");

        Path expectedFingerprint =
                Path.of(projectDirectory.toString(), "src", "test", "resources", "expected-classfile.sha256");
        String expectedContent = Files.readString(expectedFingerprint);

        assertThat(actualFingerprint).isRegularFile().hasContent(expectedContent);
    }
}
