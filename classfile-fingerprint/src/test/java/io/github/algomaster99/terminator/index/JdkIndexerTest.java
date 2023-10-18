package io.github.algomaster99.terminator.index;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.algomaster99.terminator.commons.fingerprint.ParsingHelper;
import io.github.algomaster99.terminator.commons.fingerprint.provenance.Provenance;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
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

    @Test
    void useMD5asAlgorithm(@TempDir Path tempDir) {
        // arrange
        Path indexFile = tempDir.resolve("jdk.json");

        // act
        String[] args = {"jdk", "-o", indexFile.toString(), "--algorithm", "MD5"};
        Index.main(args);

        // assert
        Map<String, List<Provenance>> referenceProvenance = ParsingHelper.deserializeFingerprints(indexFile);
        referenceProvenance.forEach((key, value) ->
                assertThat(value.get(0).classFileAttributes().algorithm()).isEqualTo("MD5"));
    }
}
