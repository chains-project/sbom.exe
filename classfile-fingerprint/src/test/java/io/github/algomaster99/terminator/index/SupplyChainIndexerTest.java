package io.github.algomaster99.terminator.index;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.algomaster99.terminator.commons.fingerprint.ParsingHelper;
import io.github.algomaster99.terminator.commons.fingerprint.provenance.Provenance;
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
        Map<String, Set<Provenance>> referenceProvenance = ParsingHelper.deserializeFingerprints(indexFile);
        assertThat(referenceProvenance.keySet().size()).isEqualTo(20826);
        referenceProvenance.forEach((key, value) -> assertThat(
                        value.stream().findAny().get().classFileAttributes().algorithm())
                .isEqualTo("MD5"));
    }
}