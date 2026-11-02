package io.github.algomaster99.terminator.commons.fingerprint;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileAttributes;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ParsingHelper {
    private static final Comparator<ClassFileAttributes> ATTRIBUTE_ORDER = Comparator.comparing(
                    ClassFileAttributes::hash)
            .thenComparing(ClassFileAttributes::classfileVersion)
            .thenComparing(ClassFileAttributes::algorithm);

    private ParsingHelper() {}

    /**
     * Writes one line per class, sorted by class name, with its attributes sorted too. The order of a hash map depends
     * on the order in which its keys were added, which depends on the order in which the JVM loaded the classes.
     */
    public static void serialiseFingerprints(Map<String, Set<ClassFileAttributes>> fingerprints, Path fingerprintFile) {
        ObjectMapper mapper = new ObjectMapper();
        try (SequenceWriter seq =
                mapper.writer().withRootValueSeparator(System.lineSeparator()).writeValues(fingerprintFile.toFile())) {
            for (var fingerprint : new TreeMap<>(fingerprints).entrySet()) {
                List<ClassFileAttributes> attributes =
                        fingerprint.getValue().stream().sorted(ATTRIBUTE_ORDER).collect(Collectors.toList());
                seq.write(Map.of(fingerprint.getKey(), attributes));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Set<ClassFileAttributes>> deserializeFingerprints(Path fingerprintFile) {
        Map<String, Set<ClassFileAttributes>> result = new HashMap<>();
        final ObjectMapper mapper = new ObjectMapper();
        try (MappingIterator<Map<String, Set<ClassFileAttributes>>> it = mapper.readerFor(
                        new TypeReference<Map<String, Set<ClassFileAttributes>>>() {})
                .readValues(fingerprintFile.toFile())) {
            while (it.hasNext()) {
                Map<String, Set<ClassFileAttributes>> item = it.nextValue();
                result.putAll(item);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
