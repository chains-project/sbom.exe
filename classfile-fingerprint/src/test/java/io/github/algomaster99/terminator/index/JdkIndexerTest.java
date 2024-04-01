package io.github.algomaster99.terminator.index;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.algomaster99.terminator.commons.fingerprint.ParsingHelper;
import io.github.algomaster99.terminator.commons.fingerprint.protobuf.Bomi;
import io.github.algomaster99.terminator.commons.fingerprint.protobuf.ClassFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
        byte[] contentFirst = Files.readAllBytes(indexFile);

        String[] argsSecond = {"jdk", "-i", indexFile.toString()};
        Index.main(argsSecond);
        byte[] contentSecond = Files.readAllBytes(indexFile);

        // assert
        assertThat(contentFirst).isEqualTo(contentSecond);
    }

    @EnabledIfSystemProperty(named = "java.vendor.version", matches = "Temurin-21\\.0\\.2\\+13")
    @Test
    void jdk21_0_2_indexShouldBeReproducible_temurin(@TempDir Path tempDir) throws IOException {
        // arrange
        Path actualIndex = tempDir.resolve("jdk.jsonl");
        Path expectedIndex = Path.of("src", "test", "resources", "jdk-index", "21.0.2-tem.bomi");

        // act
        String[] args = {"jdk", "-o", actualIndex.toString()};
        Index.main(args);

        // assert
        byte[] actual = Files.readAllBytes(actualIndex);
        byte[] expected = Files.readAllBytes(expectedIndex);
        assertThat(actual).isEqualTo(expected);

        Bomi referenceProvenance = ParsingHelper.deserializeFingerprints(actualIndex);
        for (ClassFile classFile : referenceProvenance.getClassFileList()) {
            assertThat(classFile.getAttributeCount()).isEqualTo(1);
        }
    }

    @EnabledIfSystemProperty(named = "java.vendor.version", matches = "Temurin-17\\.0\\.10\\+7")
    @Test
    void jdk17_0_10_indexShouldBeReproducibleAcrossMultiple_implementations(@TempDir Path tempDir) throws IOException {
        // arrange
        Path actualIndex = tempDir.resolve("jdk.jsonl");
        Path expectedIndex = Path.of("src", "test", "resources", "jdk-index", "17.0.10-tem.bomi");

        // act
        String[] args = {"jdk", "-o", actualIndex.toString()};
        Index.main(args);

        // assert
        byte[] actual = Files.readAllBytes(actualIndex);
        byte[] expected = Files.readAllBytes(expectedIndex);
        assertThat(Bomi.parseFrom(actual)).isEqualTo(Bomi.parseFrom(expected));

        Bomi referenceProvenance = ParsingHelper.deserializeFingerprints(actualIndex);
        for (ClassFile classFile : referenceProvenance.getClassFileList()) {
            assertThat(classFile.getAttributeCount()).isEqualTo(1);
        }
    }
}
