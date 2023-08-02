import static org.assertj.core.api.Assertions.assertThat;

import io.github.algomaster99.Options;
import io.github.algomaster99.terminator.commons.fingerprint.provenance.Jar;
import io.github.algomaster99.terminator.commons.fingerprint.provenance.Maven;
import io.github.algomaster99.terminator.commons.fingerprint.provenance.Provenance;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class OptionsTest {
    @Nested
    class ParseFingerprint {
        @Test
        void maven() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            Map<String, List<Provenance>> fingerprints =
                    deserializeFingerprints("src/test/resources/fingerprints/maven.jsonl");
            assertThat(fingerprints)
                    .extractingByKey("org/apache/commons/compress/compressors/CompressorStreamFactory")
                    .asList()
                    .hasOnlyElementsOfType(Maven.class);
        }

        @Test
        void jar() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            Map<String, List<Provenance>> fingerprints =
                    deserializeFingerprints("src/test/resources/fingerprints/jar.jsonl");
            assertThat(fingerprints)
                    .extractingByKey("org/sonar/java/checks/security/FilePermissionsCheck")
                    .asList()
                    .hasOnlyElementsOfType(Jar.class);
        }

        @Test
        void maven_jar() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            Map<String, List<Provenance>> fingerprints =
                    deserializeFingerprints("src/test/resources/fingerprints/maven_jar.jsonl");
            assertThat(fingerprints)
                    .extractingByKey("org/eclipse/jdt/core/dom/ASTNode")
                    .asList()
                    .hasSize(2)
                    .hasAtLeastOneElementOfType(Maven.class)
                    .hasAtLeastOneElementOfType(Jar.class);
        }

        private Map<String, List<Provenance>> deserializeFingerprints(String pathname)
                throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
            Method method = Options.class.getDeclaredMethod("parseFingerprints", Path.class);
            method.setAccessible(true);
            Object result = method.invoke(null, Path.of(pathname));
            return (Map<String, List<Provenance>>) result;
        }
    }
}
