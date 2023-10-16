package io.github.algomaster99.terminator.index;

import io.github.algomaster99.terminator.commons.fingerprint.JdkClass;
import io.github.algomaster99.terminator.commons.fingerprint.ParsingHelper;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileAttributes;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassfileVersion;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.HashComputer;
import io.github.algomaster99.terminator.commons.fingerprint.provenance.Jdk;
import io.github.algomaster99.terminator.commons.fingerprint.provenance.Provenance;
import java.io.File;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

@CommandLine.Command(
        name = "jdk",
        mixinStandardHelpOptions = true,
        description = "Create an index of the classfiles in JDK")
public class JdkIndexer implements Callable<Integer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Jdk.class);

    @CommandLine.ArgGroup(exclusive = true, multiplicity = "1")
    private IndexFile indexFile;

    private static class IndexFile {
        @CommandLine.Option(
                names = {"-o", "--output"},
                description = "Use this option if you want to create a new file with classfiles")
        File output;

        @CommandLine.Option(
                names = {"-i", "--input"},
                description = "Use this option if you want to use an existing file with classfiles")
        File input;
    }

    @CommandLine.Option(
            names = {"--algorithm"},
            description = "The algorithm to use for computing the hash",
            defaultValue = "SHA-256",
            required = true)
    private String algorithm;

    @Override
    public Integer call() throws Exception {
        if (indexFile.input != null) {
            Map<String, List<Provenance>> currentReferenceProvenance =
                    ParsingHelper.deserializeFingerprints(indexFile.input.toPath());
            Map<String, List<Provenance>> updatedReferenceProvenance =
                    createOrMergeProvenances(currentReferenceProvenance);
            ParsingHelper.serialiseFingerprints(updatedReferenceProvenance, indexFile.input.toPath());
            return 0;
        }
        if (indexFile.output != null) {
            Map<String, List<Provenance>> updatedReferenceProvenance = createOrMergeProvenances(new HashMap<>());
            ParsingHelper.serialiseFingerprints(updatedReferenceProvenance, indexFile.output.toPath());
            return 0;
        }
        throw new IllegalArgumentException("Either --input or --output must be specified");
    }

    public Map<String, List<Provenance>> createOrMergeProvenances(Map<String, List<Provenance>> referenceProvenance) {
        List<JdkClass> jdkClasses = io.github.algomaster99.terminator.commons.fingerprint.JdkIndexer.listJdkClasses();
        jdkClasses.forEach(resource -> {
            try {
                byte[] classfileBytes = getBytesFromBuffer(resource.bytes());
                String classfileVersion = ClassfileVersion.getVersion(classfileBytes);
                String hash = HashComputer.computeHash(classfileBytes, algorithm);
                referenceProvenance.computeIfAbsent(
                        resource.name(),
                        k -> new ArrayList<>(
                                List.of((new Jdk(new ClassFileAttributes(classfileVersion, hash, algorithm))))));
                referenceProvenance.computeIfPresent(resource.name(), (k, v) -> {
                    // TODO: should be removed after https://github.com/ASSERT-KTH/sbom.exe/issues/96 is fixed
                    if (v.stream()
                            .anyMatch(jdk -> jdk.classFileAttributes().hash().equals(hash))) {
                        return v;
                    }
                    v.add(new Jdk(new ClassFileAttributes(classfileVersion, hash, algorithm)));
                    return v;
                });
            } catch (NoSuchAlgorithmException e) {
                LOGGER.error("Failed to compute hash with algorithm: " + algorithm, e);
                throw new RuntimeException(e);
            } catch (Exception e) {
                LOGGER.error("Failed to compute hash for: " + resource, e);
            }
        });
        return referenceProvenance;
    }

    /**
     * Converts a bytebuffer to a byte array. If the buffer has an array, it returns it, otherwise it copies the bytes. This is needed because the buffer is not guaranteed to have an array.
     * See {@link java.nio.ByteBuffer#hasArray()} and {@link java.nio.DirectByteBuffer}.
     * @param buffer  the buffer to convert
     * @return  the byte array
     */
    private static byte[] getBytesFromBuffer(ByteBuffer buffer) {
        if (buffer.hasArray()) {
            return buffer.array();
        }
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }
}
