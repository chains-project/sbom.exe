import static org.assertj.core.api.Assertions.assertThat;

import io.github.algomaster99.Options;
import io.github.algomaster99.terminator.commons.fingerprint.Fingerprint;
import io.github.algomaster99.terminator.commons.fingerprint.Jar;
import io.github.algomaster99.terminator.commons.fingerprint.Maven;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class OptionsTest {
    @Nested
    class ParseFingerprint {
        @Test
        void maven() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            List<Fingerprint> fingerprints = deserializeFingerprints("src/test/resources/fingerprints/maven.jsonl");
            assertThat(fingerprints).hasOnlyElementsOfType(Maven.class).size().isEqualTo(2);
        }

        @Test
        void jar() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            List<Fingerprint> fingerprints = deserializeFingerprints("src/test/resources/fingerprints/jar.jsonl");
            assertThat(fingerprints).hasOnlyElementsOfType(Jar.class).size().isEqualTo(7);
        }

        @Test
        void maven_jar() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            List<Fingerprint> fingerprints = deserializeFingerprints("src/test/resources/fingerprints/maven_jar.jsonl");
            assertThat(fingerprints).elements(0, 2).hasOnlyElementsOfType(Maven.class);
            assertThat(fingerprints).elements(3, 5).hasOnlyElementsOfType(Jar.class);
        }

        private List<Fingerprint> deserializeFingerprints(String pathname)
                throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
            Method method = Options.class.getDeclaredMethod("parseFingerprints", File.class);
            method.setAccessible(true);
            Object result = method.invoke(null, new File(pathname));
            return (List<Fingerprint>) result;
        }
    }
}
