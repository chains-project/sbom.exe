package io.github.algomaster99;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.algomaster99.options.FromSbomOptions;
import io.github.algomaster99.terminator.commons.fingerprint.ParsingHelper;
import io.github.algomaster99.terminator.commons.fingerprint.provenance.Provenance;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FromSbomTest {

    private static final Path TEST_RESOURCES = Path.of("src", "test", "resources");

    @Test
    void guava(@TempDir Path junitTempDir) throws IOException {
        Path sbomFile = TEST_RESOURCES.resolve("guava").resolve("bom.json");

        FromSbomOptions options = getDefaultOptions(sbomFile);
        Map<String, List<Provenance>> fingerprints = FromSbom.getFingerprints(options);

        assertThat(fingerprints).size().isEqualTo(2454);

        String expectedContent =
                Files.readString(TEST_RESOURCES.resolve("guava").resolve("expected-classfile.sha256.jsonl"));
        Path actualFingerprint = junitTempDir.resolve("actual-classfile.sha256.jsonl");
        ParsingHelper.serialiseFingerprints(fingerprints, actualFingerprint);
        String actualContent = Files.readString(actualFingerprint);

        assertThat(actualContent).isEqualTo(expectedContent);
    }

    private static FromSbomOptions getDefaultOptions(Path sbomFile) throws IOException {
        return new FromSbomOptions(sbomFile, "SHA256", null);
    }
}
