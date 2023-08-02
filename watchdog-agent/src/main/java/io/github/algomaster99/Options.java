package io.github.algomaster99;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.algomaster99.terminator.commons.fingerprint.provenance.Provenance;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Options {
    private Map<String, List<Provenance>> fingerprints;

    public Options(String agentArgs) {
        String[] args = agentArgs.split(",");
        for (String arg : args) {
            String[] split = arg.split("=");
            if (split.length != 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            String key = split[0];
            String value = split[1];

            switch (key) {
                case "fingerprints":
                    fingerprints = parseFingerprints(Path.of(value));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown argument: " + key);
            }
        }
    }

    private static Map<String, List<Provenance>> parseFingerprints(Path fingerprintFile) {
        Map<String, List<Provenance>> result = new HashMap<>();
        final ObjectMapper mapper = new ObjectMapper();
        try (MappingIterator<Map<String, List<Provenance>>> it =
                mapper.readerFor(Map.class).readValues(fingerprintFile.toFile())) {
            while (it.hasNext()) {
                Map<String, List<Provenance>> item = it.nextValue();
                result.putAll(item);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public Map<String, List<Provenance>> getFingerprints() {
        if (fingerprints == null) {
            throw new IllegalStateException("Fingerprints not set");
        }
        return fingerprints;
    }
}
