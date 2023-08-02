package io.github.algomaster99.it;

import static com.soebes.itf.extension.assertj.MavenITAssertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soebes.itf.jupiter.extension.MavenGoal;
import com.soebes.itf.jupiter.extension.MavenGoals;
import com.soebes.itf.jupiter.extension.MavenJupiterExtension;
import com.soebes.itf.jupiter.extension.MavenOption;
import com.soebes.itf.jupiter.extension.MavenTest;
import com.soebes.itf.jupiter.maven.MavenExecutionResult;
import io.github.algomaster99.terminator.commons.fingerprint.ParsingHelper;
import io.github.algomaster99.terminator.commons.fingerprint.provenance.Provenance;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

@MavenGoals(
        value = {
            @MavenGoal("compile"),
            @MavenGoal("${project.groupId}:${project.artifactId}:${project.version}:generate")
        })
@MavenJupiterExtension
class GenerateMojoIT {

    // @MavenGoals was not inherited by nested classes, so we have to repeat it here
    @Nested
    @MavenGoals(
            value = {
                @MavenGoal("compile"),
                @MavenGoal("${project.groupId}:${project.artifactId}:${project.version}:generate")
            })
    class Algorithm {
        private static final int CLASSFILES_LOADED = 5991;

        @DisplayName("SHA256 - the default algorithm")
        @MavenTest
        void sha256(MavenExecutionResult result) {
            assertThat(result).isSuccessful();

            Path fingerprintFile = getFingerprint(
                    result.getMavenProjectResult().getTargetProjectDirectory(), "classfile.sha256.jsonl");

            assertThat(fingerprintFile).isRegularFile().content().hasLineCount(CLASSFILES_LOADED);
            assertAlgorithm(fingerprintFile.toFile(), "SHA256");
        }

        @DisplayName("SHA1")
        @MavenOption("-Dalgorithm=SHA1")
        @MavenTest
        void sha1(MavenExecutionResult result) {
            assertThat(result).isSuccessful();

            Path fingerprintFile =
                    getFingerprint(result.getMavenProjectResult().getTargetProjectDirectory(), "classfile.sha1.jsonl");

            assertThat(fingerprintFile).isRegularFile().content().hasLineCount(CLASSFILES_LOADED);
            assertAlgorithm(fingerprintFile.toFile(), "SHA1");
        }

        @DisplayName("MD5")
        @MavenOption("-Dalgorithm=MD5")
        @MavenTest
        void md5(MavenExecutionResult result) {
            assertThat(result).isSuccessful();

            Path fingerprintFile =
                    getFingerprint(result.getMavenProjectResult().getTargetProjectDirectory(), "classfile.md5.jsonl");

            assertThat(fingerprintFile).isRegularFile().content().hasLineCount(CLASSFILES_LOADED);
            assertAlgorithm(fingerprintFile.toFile(), "MD5");
        }

        private static void assertAlgorithm(File fingerprintsFile, String algorithm) {
            final ObjectMapper mapper = new ObjectMapper();
            try (MappingIterator<Map<String, List<Provenance>>> it = mapper.readerFor(
                            new TypeReference<Map<String, List<Provenance>>>() {})
                    .readValues(fingerprintsFile)) {
                while (it.hasNext()) {
                    Map<String, List<Provenance>> item = it.nextValue();
                    for (String className : item.keySet()) {
                        List<Provenance> provenances = item.get(className);
                        provenances.forEach(p ->
                                assertThat(p.classFileAttributes().algorithm()).isEqualTo(algorithm));
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @MavenTest
    void guava(MavenExecutionResult result) throws IOException {
        assertThat(result).isSuccessful();

        Path projectDirectory = result.getMavenProjectResult().getTargetProjectDirectory();

        Path actualFingerprint = getFingerprint(projectDirectory, "classfile.sha256.jsonl");

        Path expectedFingerprint =
                Path.of(projectDirectory.toString(), "src", "test", "resources", "expected-classfile.sha256.jsonl");
        String expectedContent = Files.readString(expectedFingerprint);

        assertThat(actualFingerprint).isRegularFile().hasContent(expectedContent);
    }

    @DisplayName("Classfile fingerprint with native dependency")
    @MavenTest
    void native_dependency(MavenExecutionResult result) throws IOException {
        assertThat(result).isSuccessful();

        String osName = System.getProperty("os.name");

        Path projectDirectory = result.getMavenProjectResult().getTargetProjectDirectory();

        Path actualFingerprint = getFingerprint(projectDirectory, "classfile.sha256.jsonl");

        Path expectedFingerprint;

        if (osName.contains("Mac")) {
            expectedFingerprint = Path.of(
                    projectDirectory.toString(), "src", "test", "resources", "expected-classfile.MacOS.sha256.jsonl");
        } else if (osName.contains("Linux")) {
            expectedFingerprint = Path.of(
                    projectDirectory.toString(), "src", "test", "resources", "expected-classfile.Linux.sha256.jsonl");
        } else if (osName.contains("Windows")) {
            expectedFingerprint = Path.of(
                    projectDirectory.toString(), "src", "test", "resources", "expected-classfile.Windows.sha256.jsonl");
        } else {
            throw new IllegalStateException("Unsupported OS: " + osName);
        }

        String expectedContent = Files.readString(expectedFingerprint);

        assertThat(actualFingerprint).isRegularFile().hasContent(expectedContent);
    }

    @DisplayName("Different fingerprint should be generated for sub-modules")
    @MavenTest
    void multi_module(MavenExecutionResult result) {
        assertThat(result).isSuccessful();

        Path rootModule = result.getMavenProjectResult().getTargetProjectDirectory();

        Path a = Path.of(rootModule.toString(), "a");
        Path b = Path.of(rootModule.toString(), "b");

        Path rootFingerPrint = getFingerprint(rootModule, "classfile.sha256.jsonl");
        assertThat(rootFingerPrint).isRegularFile().isEmptyFile();

        Path aFingerPrint = getFingerprint(a, "classfile.sha256.jsonl");
        assertThat(aFingerPrint).isRegularFile().isNotEmptyFile();

        Path bFingerPrint = getFingerprint(b, "classfile.sha256.jsonl");
        assertThat(bFingerPrint).isRegularFile().isNotEmptyFile();
    }

    @DisplayName("Report classfile's fingerprint of another submodule")
    @MavenTest
    void multi_module_with_sources(MavenExecutionResult result) throws IOException {
        assertThat(result).isSuccessful();

        Path rootModule = result.getMavenProjectResult().getTargetProjectDirectory();

        Path main = Path.of(rootModule.toString(), "main");
        Path fingerPrint = getFingerprint(main, "classfile.sha256.jsonl");

        Path expectedFingerprint =
                Path.of(main.toString(), "src", "test", "resources", "expected-classfile.sha256.jsonl");

        assertThat(fingerPrint).isRegularFile().hasContent(Files.readString(expectedFingerprint));
    }

    @MavenTest
    @MavenOption("-DexternalJars=src/test/resources/externalJars.json")
    void url_classloader_local_jar(MavenExecutionResult result) {
        assertThat(result).isSuccessful();

        Path projectDirectory = result.getMavenProjectResult().getTargetProjectDirectory();
        Path fingerprint = getFingerprint(projectDirectory, "classfile.sha256.jsonl");
        Map<String, List<Provenance>> fingerprints = ParsingHelper.deserializeFingerprints(fingerprint);

        assertThat(fingerprints)
                .hasSize(2)
                .extractingByKey("NonMalicious")
                .asList()
                .element(0)
                .extracting("classFileAttributes")
                .hasFieldOrPropertyWithValue(
                        "hash", "de3318e0ba5527a90fb600307ca12e0d06752474d1da3086cfdb4a48f714da5d");
    }

    private static Path getFingerprint(Path projectDirectory, String classfileFingerprintName) {
        return Path.of(projectDirectory.toString(), "target", classfileFingerprintName);
    }
}
