package io.github.algomaster99.terminator.commons.fingerprint;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JdkDigestTest {

    @Test
    void digestIgnoresSkippedFiles(@TempDir Path javaHome) throws IOException {
        // arrange
        write(javaHome.resolve("lib/modules"), "modules");
        String original = JdkDigest.compute(javaHome, "SHA-256");

        // act
        write(javaHome.resolve("conf/security/java.security"), "edited config");
        write(javaHome.resolve("legal/java.base/LICENSE"), "license");
        write(javaHome.resolve("lib/src.zip"), "sources");

        // assert
        assertThat(JdkDigest.compute(javaHome, "SHA-256")).isEqualTo(original);
    }

    @Test
    void digestChangesWithAnyOtherFile(@TempDir Path javaHome) throws IOException {
        // arrange
        write(javaHome.resolve("lib/modules"), "modules");
        write(javaHome.resolve("lib/server/classes.jsa"), "cds");
        String original = JdkDigest.compute(javaHome, "SHA-256");

        // act and assert
        write(javaHome.resolve("lib/server/classes.jsa"), "tampered cds");
        assertThat(JdkDigest.compute(javaHome, "SHA-256")).isNotEqualTo(original);
        write(javaHome.resolve("lib/server/classes.jsa"), "cds");
        assertThat(JdkDigest.compute(javaHome, "SHA-256")).isEqualTo(original);

        // A file that no allow-list would name, like the helper that the JVM runs to start processes.
        write(javaHome.resolve("lib/jspawnhelper"), "helper");
        assertThat(JdkDigest.compute(javaHome, "SHA-256")).isNotEqualTo(original);
    }

    @Test
    void versionComesFromTheReleaseFile(@TempDir Path javaHome) throws IOException {
        write(javaHome.resolve("release"), "JAVA_RUNTIME_VERSION=\"21.0.6+7-LTS\"\n");
        assertThat(JdkDigest.version(javaHome)).isEqualTo("21.0.6+7-LTS");
    }

    private static void write(Path file, String content) throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);
    }
}
