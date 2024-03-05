package io.github.algomaster99.terminator.index;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.algomaster99.terminator.commons.fingerprint.ParsingHelper;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileAttributes;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.JRE;
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
    }

    @EnabledOnJre(JRE.JAVA_21)
    @Test
    void jdkIndexShouldHave__jdk_internal_foreign_MemorySessionImpl__and__java_lang_foreign_MemorySegment$Scope(
            @TempDir Path tempDir) {
        // arrange
        Path indexFile = tempDir.resolve("jdk.json");

        // act
        String[] args = {"jdk", "-o", indexFile.toString()};
        Index.main(args);

        // assert
        Map<String, Set<ClassFileAttributes>> referenceProvenance = ParsingHelper.deserializeFingerprints(indexFile);
        assertThat(referenceProvenance)
                .containsKeys("jdk/internal/foreign/MemorySessionImpl", "java/lang/foreign/MemorySegment$Scope");
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
