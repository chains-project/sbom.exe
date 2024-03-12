package io.github.algomaster99.terminator.index;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.algomaster99.terminator.commons.fingerprint.ParsingHelper;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileAttributes;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;

class JdkIndexerTest {
    @Test
    void jdkIndexShouldBeDeterministic_irrespectiveOfTheJDK(@TempDir Path tempDir) throws IOException {
        // arrange
        Path indexFile = tempDir.resolve("jdk.json");

        // act
        String[] argsFirst = {"jdk", "-o", indexFile.toString()};
        Index.main(argsFirst);
        String contentFirst = Files.readString(indexFile);

        String[] argsSecond = {"jdk", "-i", indexFile.toString()};
        Index.main(argsSecond);
        String contentSecond = Files.readString(indexFile);

        // assert
        assertThat(contentFirst).isEqualTo(contentSecond);

        System.out.println(System.getProperty("java.vendor.version"));
    }

    @EnabledIfSystemProperty(named = "java.vendor.version", matches = "Temurin-21.0.2+13")
    @Test
    void jdk21_0_2_indexShouldBeReproducible_temurin(@TempDir Path tempDir) throws IOException {
        // arrange
        Path actualIndex = tempDir.resolve("jdk.jsonl");
        Path expectedIndex = Path.of("src", "test", "resources", "jdk-index", "21.0.2-tem.jsonl");

        // act
        String[] args = {"jdk", "-o", actualIndex.toString()};
        Index.main(args);

        // assert
        List<String> actual = Files.readAllLines(actualIndex);
        List<String> expected = Files.readAllLines(expectedIndex);
        assertThat(actual).isEqualTo(expected);

        Map<String, Set<ClassFileAttributes>> referenceProvenance = ParsingHelper.deserializeFingerprints(actualIndex);
        referenceProvenance.values().forEach(classFileAttributes -> assertThat(classFileAttributes.size())
                .isEqualTo(1));
    }

    @EnabledIfSystemProperty(named = "java.vendor.version", matches = "Temurin-17.0.10+7")
    @Test
    void jdk17_0_10_indexShouldBeReproducibleAcrossMultiple_implementations(@TempDir Path tempDir) throws IOException {
        // arrange
        Path actualIndex = tempDir.resolve("jdk.jsonl");
        Path expectedIndex = Path.of("src", "test", "resources", "jdk-index", "17.0.10-tem.jsonl");

        // act
        String[] args = {"jdk", "-o", actualIndex.toString()};
        Index.main(args);

        // assert
        List<String> actual = Files.readAllLines(actualIndex);
        List<String> expected = Files.readAllLines(expectedIndex);
        assertThat(actual).isEqualTo(expected);

        Map<String, Set<ClassFileAttributes>> referenceProvenance = ParsingHelper.deserializeFingerprints(actualIndex);
        referenceProvenance.values().forEach(classFileAttributes -> assertThat(classFileAttributes.size())
                .isEqualTo(1));
    }

    @Test
    void useMD5asAlgorithm(@TempDir Path tempDir) {
        // arrange
        Path indexFile = tempDir.resolve("jdk.json");

        // act
        String[] args = {"jdk", "-o", indexFile.toString(), "--algorithm", "MD5"};
        Index.main(args);

        // assert
        Map<String, Set<ClassFileAttributes>> referenceProvenance = ParsingHelper.deserializeFingerprints(indexFile);
        referenceProvenance.forEach((key, value) ->
                assertThat(value.stream().findAny().get().algorithm()).isEqualTo("MD5"));
    }
}
