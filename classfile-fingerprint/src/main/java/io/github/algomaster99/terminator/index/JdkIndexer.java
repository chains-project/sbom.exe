package io.github.algomaster99.terminator.index;

import io.github.algomaster99.terminator.commons.fingerprint.JdkClass;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileAttributes;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassfileVersion;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.HashComputer;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

@CommandLine.Command(
        name = "jdk",
        mixinStandardHelpOptions = true,
        description = "Create an index of the classfiles in JDK")
public class JdkIndexer extends BaseIndexer implements Callable<Integer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdkIndexer.class);

    Map<String, Set<ClassFileAttributes>> createOrMergeProvenances(
            Map<String, Set<ClassFileAttributes>> referenceProvenance) {
        List<JdkClass> jdkClasses = io.github.algomaster99.terminator.commons.fingerprint.JdkIndexer.listJdkClasses();
        jdkClasses.forEach(resource -> {
            try {
                byte[] classfileBytes = getBytesFromBuffer(resource.bytes());
                String classfileVersion = ClassfileVersion.getVersion(classfileBytes);
                String hash = HashComputer.computeHash(classfileBytes, algorithm);
                referenceProvenance.computeIfAbsent(
                        resource.name(),
                        k -> new HashSet<>(Set.of(new ClassFileAttributes(classfileVersion, hash, algorithm))));
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
