package io.github.algomaster99.terminator.commons.fingerprint;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JdkDigestTest {

    @Test
    void digestChangesWithCode_butNotWithConfiguration(@TempDir Path javaHome) throws IOException {
        // arrange
        write(javaHome.resolve("lib/modules"), "modules");
        write(javaHome.resolve("lib/server/libjvm.so"), "jvm");
        write(javaHome.resolve("lib/server/classes.jsa"), "cds");
        write(javaHome.resolve("conf/security/java.security"), "config");
        write(javaHome.resolve("release"), "JAVA_RUNTIME_VERSION=\"21.0.6+7-LTS\"\n");
        String original = JdkDigest.compute(javaHome, "SHA-256");

        // act and assert
        write(javaHome.resolve("conf/security/java.security"), "edited config");
        assertThat(JdkDigest.compute(javaHome, "SHA-256")).isEqualTo(original);

        write(javaHome.resolve("lib/server/classes.jsa"), "tampered cds");
        assertThat(JdkDigest.compute(javaHome, "SHA-256")).isNotEqualTo(original);
        write(javaHome.resolve("lib/server/classes.jsa"), "cds");
        assertThat(JdkDigest.compute(javaHome, "SHA-256")).isEqualTo(original);

        write(javaHome.resolve("lib/libevil.so"), "added native code");
        assertThat(JdkDigest.compute(javaHome, "SHA-256")).isNotEqualTo(original);

        assertThat(JdkDigest.version(javaHome)).isEqualTo("21.0.6+7-LTS");
    }

    private static void write(Path file, String content) throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);
    }
}
