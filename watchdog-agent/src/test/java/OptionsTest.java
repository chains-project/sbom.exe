import static org.assertj.core.api.Assertions.assertThat;

import io.github.algomaster99.Options;
import io.github.algomaster99.terminator.commons.fingerprint.Fingerprint;
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
            Method method = Options.class.getDeclaredMethod("parseFingerprints", File.class);
            method.setAccessible(true);
            Object result = method.invoke(null, new File("src/test/resources/fingerprints/maven.jsonl"));

            List<Fingerprint> fingerprints = (List<Fingerprint>) result;
            assertThat(fingerprints).hasOnlyElementsOfType(Maven.class).size().isEqualTo(2);
        }
    }
}
