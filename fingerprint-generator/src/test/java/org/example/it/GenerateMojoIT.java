package org.example.it;

import static com.soebes.itf.extension.assertj.MavenITAssertions.assertThat;

import com.soebes.itf.jupiter.extension.MavenGoal;
import com.soebes.itf.jupiter.extension.MavenJupiterExtension;
import com.soebes.itf.jupiter.extension.MavenTest;
import com.soebes.itf.jupiter.maven.MavenExecutionResult;

@MavenJupiterExtension
class GenerateMojoIT {
    @MavenGoal("${project.groupId}:${project.artifactId}:${project.version}:generate")
    @MavenTest
    void hello_world(MavenExecutionResult result) {
        assertThat(result).isSuccessful();
    }
}
