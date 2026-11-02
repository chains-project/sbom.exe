package io.github.algomaster99.terminator.index;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.algomaster99.terminator.commons.fingerprint.JdkDigest;
import io.github.algomaster99.terminator.commons.fingerprint.ParsingHelper;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileAttributes;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JdkIndexerTest {
    @Test
    void jdkIndexShouldBeDeterministic_irrespectiveOfTheJDK(@TempDir Path tempDir) throws IOException {
        // arrange
        Path indexFile = tempDir.resolve("jdk.json");

        // act
        String[] argsFirst = {"jdk", "-o", indexFile.toString()};
        assertThat(Index.execute(argsFirst)).isZero();
        String contentFirst = Files.readString(indexFile);

        String[] argsSecond = {"jdk", "-i", indexFile.toString()};
        assertThat(Index.execute(argsSecond)).isZero();
        String contentSecond = Files.readString(indexFile);

        // assert
        assertThat(contentFirst).isEqualTo(contentSecond);
    }

    @Test
    void eachJdkAddsOneDigest(@TempDir Path tempDir) throws IOException {
        // arrange
        Path indexFile = tempDir.resolve("jdk.jsonl");
        Path otherJdk = tempDir.resolve("other-jdk");
        Files.createDirectories(otherJdk.resolve("lib"));
        Files.writeString(otherJdk.resolve("lib/modules"), "modules of another JDK");
        Path runningJdk = Path.of(System.getProperty("java.home"));

        // act
        assertThat(Index.execute(new String[] {"jdk", "-o", indexFile.toString()}))
                .isZero();
        assertThat(Index.execute(new String[] {"jdk", "--java-home", otherJdk.toString(), "-i", indexFile.toString()}))
                .isZero();

        // assert
        // All JDKs share one key, like the versions of a class: its value holds one digest per JDK.
        Map<String, Set<ClassFileAttributes>> referenceProvenance = ParsingHelper.deserializeFingerprints(indexFile);
        assertThat(referenceProvenance).hasSize(1).containsOnlyKeys(JdkDigest.BOMI_KEY);
        assertThat(referenceProvenance.get(JdkDigest.BOMI_KEY))
                .hasSize(2)
                .extracting(ClassFileAttributes::hash)
                .containsExactlyInAnyOrder(
                        JdkDigest.compute(runningJdk, "SHA-256"), JdkDigest.compute(otherJdk, "SHA-256"));
    }

    @Test
    void useMD5asAlgorithm(@TempDir Path tempDir) {
        // arrange
        Path indexFile = tempDir.resolve("jdk.json");

        // act
        String[] args = {"jdk", "-o", indexFile.toString(), "--algorithm", "MD5"};
        assertThat(Index.execute(args)).isZero();

        // assert
        Map<String, Set<ClassFileAttributes>> referenceProvenance = ParsingHelper.deserializeFingerprints(indexFile);
        referenceProvenance.forEach((key, value) ->
                assertThat(value.stream().findAny().get().algorithm()).isEqualTo("MD5"));
    }
}
