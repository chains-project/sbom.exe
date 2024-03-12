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
import org.junit.jupiter.api.io.TempDir;

class SupplyChainIndexerTest {
    @Test
    void supplyChainIndexShouldBeDeterministic(@TempDir Path tempDir) throws IOException {
        // arrange
        Path indexFile = tempDir.resolve("sc.json");
        Path sbom = Path.of("src", "test", "resources", "pdfbox-3.0.0.build-info-go-1.9.14.json");

        // act
        String[] argsFirst = {"supply-chain", "-s", sbom.toString(), "-o", indexFile.toString()};
        Index.main(argsFirst);
        String contentFirst = Files.readString(indexFile);

        String[] argsSecond = {"supply-chain", "-s", sbom.toString(), "-i", indexFile.toString()};
        Index.main(argsSecond);
        String contentSecond = Files.readString(indexFile);

        // assert
        assertThat(contentFirst).isEqualTo(contentSecond);
    }

    @Test
    void useMD5asAlgorithm(@TempDir Path tempDir) {
        // arrange
        Path indexFile = tempDir.resolve("sc.json");
        Path sbom = Path.of("src", "test", "resources", "pdfbox-3.0.0.build-info-go-1.9.14.json");

        // act
        String[] args = {"supply-chain", "-s", sbom.toString(), "-o", indexFile.toString(), "--algorithm", "MD5"};
        Index.main(args);

        // assert
        Map<String, Set<ClassFileAttributes>> referenceProvenance = ParsingHelper.deserializeFingerprints(indexFile);
        assertThat(referenceProvenance.keySet().size()).isEqualTo(18929);
        referenceProvenance.forEach((key, value) ->
                assertThat(value.stream().findAny().get().algorithm()).isEqualTo("MD5"));
    }

    @Test
    void getAllClassesIn_log4jCore_log4jApi(@TempDir Path tempDir) {
        // arrange
        Path indexFile = tempDir.resolve("sc.json");
        Path sbom = Path.of("src", "test", "resources", "supply-chain-index", "log4j-core.bom.json");

        // act
        String[] args = {"supply-chain", "-s", sbom.toString(), "-o", indexFile.toString()};
        Index.main(args);

        // assert
        Map<String, Set<ClassFileAttributes>> referenceProvenance = ParsingHelper.deserializeFingerprints(indexFile);
        assertThat(referenceProvenance.keySet().size()).isEqualTo(1273);
    }
}
