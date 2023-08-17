import static org.assertj.core.api.Assertions.assertThat;

import io.github.algomaster99.terminator.commons.cyclonedx.Bom14Schema;
import io.github.algomaster99.terminator.commons.cyclonedx.CycloneDX;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

public class CycloneDXTest {
    @Test
    void getPOJO_shouldGenerateJavaAPIsToParseSBOM() throws IOException {
        Path bom = Paths.get("src/test/resources/cyclonedx/terminator-0.8.1-SNAPSHOT.bom.json");
        String bomString = Files.readString(bom);

        Bom14Schema bom14Schema = CycloneDX.getPOJO(bomString);

        String groupId = bom14Schema.getMetadata().getComponent().getGroup();
        assertThat(groupId).isEqualTo("io.github.algomaster99");

        String artifactId = bom14Schema.getMetadata().getComponent().getName();
        assertThat(artifactId).isEqualTo("terminator");
    }
}
