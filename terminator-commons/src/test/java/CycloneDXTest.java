import static org.assertj.core.api.Assertions.assertThat;

import io.github.algomaster99.terminator.commons.cyclonedx.CycloneDX;
import io.github.algomaster99.terminator.commons.cyclonedx.CycloneDXWrapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

public class CycloneDXTest {
    @Test
    void getPojo_1_4_CycloneDX_1_4_shouldGenerateJavaAPIsToParseSBOM() throws IOException {
        Path bom = Paths.get("src/test/resources/cyclonedx/terminator-0.8.1-SNAPSHOT.bom.json");
        String bomString = Files.readString(bom);

        CycloneDXWrapper bom14Schema = CycloneDX.getPojo(bomString);

        String groupId = bom14Schema.getMetadata().getComponent().getGroup();
        assertThat(groupId).isEqualTo("io.github.algomaster99");

        String artifactId = bom14Schema.getMetadata().getComponent().getName();
        assertThat(artifactId).isEqualTo("terminator");
    }

    @Test
    void getPojo_1_5_CycloneDX_1_5_shouldGenerateJavaAPIsToParseSBOM() throws IOException {
        Path bom = Paths.get("src/test/resources/cyclonedx/pdfbox-3.0.0.build-info-go-1.9.14.json");
        String bomString = Files.readString(bom);

        CycloneDXWrapper cycloneDXBom = CycloneDX.getPojo(bomString);

        assertThat(cycloneDXBom.getComponents().size()).isEqualTo(48);
    }
}
