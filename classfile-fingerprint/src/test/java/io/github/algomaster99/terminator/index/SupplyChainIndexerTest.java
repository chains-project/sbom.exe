package io.github.algomaster99.terminator.index;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.algomaster99.terminator.commons.fingerprint.ParsingHelper;
import io.github.algomaster99.terminator.commons.fingerprint.protobuf.Bomi;
import io.github.algomaster99.terminator.commons.fingerprint.protobuf.ClassFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
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
        byte[] contentFirst = Files.readAllBytes(indexFile);

        String[] argsSecond = {"supply-chain", "-s", sbom.toString(), "-i", indexFile.toString()};
        Index.main(argsSecond);
        byte[] contentSecond = Files.readAllBytes(indexFile);

        // assert
        assertThat(Bomi.parseFrom(contentFirst)).isEqualTo(Bomi.parseFrom(contentSecond));
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
        Bomi referenceProvenance = ParsingHelper.deserializeFingerprints(indexFile);
        assertThat(referenceProvenance.getClassFileCount()).isEqualTo(1269);

        for (ClassFile referenceProvenanceEntry : referenceProvenance.getClassFileList()) {
            if (referenceProvenanceEntry.getClassName().equals("org/apache/logging/log4j/util/StackLocator")) {
                assertThat(referenceProvenanceEntry.getAttributeCount()).isEqualTo(2);
                assertThat(referenceProvenanceEntry.getAttributeList().stream()
                                .map(ClassFile.Attribute::getVersion)
                                .collect(Collectors.toSet()))
                        .contains("52.0", "53.0");
            }
            // log4j-core has two versions of SystemClock
            // one as is and one in META-INF/versions/9/org/apache/logging/log4j/core/util/SystemClock
            // Even though they are kept in different directories, they have the same classfile version
            if (referenceProvenanceEntry.getClassName().equals("org/apache/logging/log4j/core/util/SystemClock")) {
                assertThat(referenceProvenanceEntry.getAttributeCount()).isEqualTo(2);
                assertThat(referenceProvenanceEntry.getAttributeList().stream()
                                .map(ClassFile.Attribute::getVersion)
                                .collect(Collectors.toSet()))
                        .contains("52.0");
            }
        }
    }
}
