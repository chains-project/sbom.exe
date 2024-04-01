package io.github.algomaster99.terminator.commons.fingerprint;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileAttributes;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ParsingHelper {
    private ParsingHelper() {}

    public static void serialiseFingerprints(Map<String, Set<ClassFileAttributes>> fingerprints, Path fingerprintFile) {
        ObjectMapper mapper = new ObjectMapper();
        try (SequenceWriter seq =
                mapper.writer().withRootValueSeparator(System.lineSeparator()).writeValues(fingerprintFile.toFile())) {
            for (var fingerprint : fingerprints.entrySet()) {
                seq.write(fingerprint);
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
