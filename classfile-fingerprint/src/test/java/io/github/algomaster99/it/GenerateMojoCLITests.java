package io.github.algomaster99.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * These tests do not fit the maven-it-extension model, so we use the maven-invoker-plugin directly.
 */
class GenerateMojoCLITests {
    @Test
    void projectUsingACustomLocalRepository(@TempDir Path tempDir) throws IOException, MavenInvocationException {
        Path testDirectory =
                Path.of("src", "test", "resources", "prioritise-target-classes").toAbsolutePath();

        FileUtils.copyDirectory(testDirectory.toFile(), tempDir.toFile());
        Path userSettings = tempDir.resolve("settings.xml").toAbsolutePath();

        // Invoke compile
        InvocationRequest compileRequest = new DefaultInvocationRequest();
        compileRequest.setPomFile(tempDir.resolve("pom.xml").toFile());
        compileRequest.setUserSettingsFile(userSettings.toFile());
        compileRequest.setGoals(List.of("compile"));
        Invoker compileInvoker = new DefaultInvoker();
        compileInvoker.execute(compileRequest);

        // Invoke generate
        InvocationRequest fingerprintRequest = new DefaultInvocationRequest();
        fingerprintRequest.setPomFile(tempDir.resolve("pom.xml").toFile());
        fingerprintRequest.setUserSettingsFile(userSettings.toFile());
        fingerprintRequest.setDebug(true);
        fingerprintRequest.setGoals(List.of("io.github.algomaster99:classfile-fingerprint:0.5.0:generate"));
        Invoker fingerprintInvoker = new DefaultInvoker();
        InvocationResult result = fingerprintInvoker.execute(fingerprintRequest);

        Path actualFingerprintFile =
                tempDir.resolve("foobar-main").resolve("target").resolve("classfile.sha256.jsonl");
        Path expectedFingerprintFile = tempDir.resolve("foobar-main")
                .resolve("src")
                .resolve("test")
                .resolve("resources")
                .resolve("expected-classfile.sha256.jsonl");

        assertThat(actualFingerprintFile).hasContent(Files.readString(expectedFingerprintFile));
    }
}
