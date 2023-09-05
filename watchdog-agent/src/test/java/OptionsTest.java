import static io.github.algomaster99.terminator.commons.fingerprint.ParsingHelper.deserializeFingerprints;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.algomaster99.Options;
import io.github.algomaster99.terminator.commons.fingerprint.provenance.Jar;
import io.github.algomaster99.terminator.commons.fingerprint.provenance.Maven;
import io.github.algomaster99.terminator.commons.fingerprint.provenance.Provenance;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class OptionsTest {
    @Nested
    class ParseFingerprint {
        @Test
        void maven() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            Map<String, List<Provenance>> fingerprints =
                    deserializeFingerprints(Path.of("src/test/resources/fingerprints/maven.jsonl"));
            assertThat(fingerprints)
                    .extractingByKey("org/apache/commons/compress/compressors/CompressorStreamFactory")
                    .asList()
                    .hasOnlyElementsOfType(Maven.class);
        }

        @Test
        void jar() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            Map<String, List<Provenance>> fingerprints =
                    deserializeFingerprints(Path.of("src/test/resources/fingerprints/jar.jsonl"));
            assertThat(fingerprints)
                    .extractingByKey("org/sonar/java/checks/security/FilePermissionsCheck")
                    .asList()
                    .hasOnlyElementsOfType(Jar.class);
        }

        @Test
        void maven_jar() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            Map<String, List<Provenance>> fingerprints =
                    deserializeFingerprints(Path.of("src/test/resources/fingerprints/maven_jar.jsonl"));
            assertThat(fingerprints)
                    .extractingByKey("org/eclipse/jdt/core/dom/ASTNode")
                    .asList()
                    .hasSize(2)
                    .hasAtLeastOneElementOfType(Maven.class)
                    .hasAtLeastOneElementOfType(Jar.class);
        }
    }

    @Test
    void verifyIfJdkIndexerFindsJdkClassesDeterministically() throws Exception {
        // generating 2 times the jdk fingerprint should result in the same fingerprint
        Options options = new Options("skipShutdown=true");
        Options options2 = new Options("skipShutdown=true");
        assertThat(options.getJdkFingerprints()).isNotEmpty();
        assertThat(options2.getJdkFingerprints()).isNotEmpty();
        assertThat(options.getJdkFingerprints()).isEqualTo(options2.getJdkFingerprints());
        System.out.println(options.getJdkFingerprints().size());
    }

    @Test
    void verifyJdkIndexerFindsOrgXmlSax() throws Exception {
        Options options = new Options("skipShutdown=true");
        var var = options.getJdkFingerprints().keySet().stream().collect(Collectors.toSet());
        assertThat(var).contains("org/xml/sax/helpers/NamespaceSupport");
    }

    @Test
    void verifyJdkIndexerFindsMethodHandle() throws Exception {
        Options options = new Options("skipShutdown=true");
        boolean containsKey = options.getJdkFingerprints().containsKey("java/lang/invoke/BoundMethodHandle");
        assertThat(containsKey).isTrue();
    }
}
