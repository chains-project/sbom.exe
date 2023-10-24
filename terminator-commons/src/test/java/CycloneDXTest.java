import static org.assertj.core.api.Assertions.assertThat;

import io.github.algomaster99.terminator.commons.cyclonedx.Bom14Schema;
import io.github.algomaster99.terminator.commons.cyclonedx.Bom15Schema;
import io.github.algomaster99.terminator.commons.cyclonedx.CycloneDX;
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

        Bom14Schema bom14Schema = CycloneDX.getPojo_1_4(bomString);

        String groupId = bom14Schema.getMetadata().getComponent().getGroup();
        assertThat(groupId).isEqualTo("io.github.algomaster99");

        String artifactId = bom14Schema.getMetadata().getComponent().getName();
        assertThat(artifactId).isEqualTo("terminator");
    }

    @Test
    void getPojo_1_5_CycloneDX_1_5_shouldGenerateJavaAPIsToParseSBOM() throws IOException {
        Path bom = Paths.get("src/test/resources/cyclonedx/pdfbox-3.0.0.build-info-go-1.9.14.json");
        String bomString = Files.readString(bom);

        Bom15Schema bom15Schema = CycloneDX.getPojo_1_5(bomString);

        assertThat(bom15Schema.getComponents().size()).isEqualTo(48);
    }
}
