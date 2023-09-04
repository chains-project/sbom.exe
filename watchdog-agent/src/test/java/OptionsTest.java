import static io.github.algomaster99.terminator.commons.fingerprint.ParsingHelper.deserializeFingerprints;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.algomaster99.Options;
import io.github.algomaster99.terminator.commons.fingerprint.provenance.Jar;
import io.github.algomaster99.terminator.commons.fingerprint.provenance.Maven;
import io.github.algomaster99.terminator.commons.fingerprint.provenance.Provenance;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nonapi.io.github.classgraph.classpath.SystemJarFinder;
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

        @Test
        void parse_jdk() throws Exception {
            // generating 2 times the jdk fingerprint should result in the same fingerprint
            Options options = new Options("skipShutdown=true");
            Options options2 = new Options("skipShutdown=true");
            assertThat(options.getJdkFingerprints()).isNotEmpty();
            assertThat(options2.getJdkFingerprints()).isNotEmpty();
            assertThat(options.getJdkFingerprints()).isEqualTo(options2.getJdkFingerprints());
        }
    }

    public static void main(String[] args) {
        Set<String> systemJarFinder = SystemJarFinder.getJreLibOrExtJars();
        systemJarFinder.forEach(System.out::println);
        ScanResult scanResult = new ClassGraph()
                .enableAllInfo()
                .enableSystemJarsAndModules()
                .acceptLibOrExtJars()
                .acceptPackages("java.*", "jdk.*", "sun.*", "oracle.*", "javax.*", "*")
                .ignoreClassVisibility()
                .enableMemoryMapping()
                .scan();
        System.out.println(scanResult.getAllClasses().size());
        var result = scanResult.getAllClasses().stream()
                        .filter(v -> v.getName().contains("JrtFileSystemProvider"))
                //.filter(v -> v.getClassfileMajorVersion() != 64)
                .toList();
        System.out.println(result.stream().map(v -> v.getClassfileMajorVersion()).toList());
    }
}
