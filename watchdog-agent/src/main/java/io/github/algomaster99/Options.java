package io.github.algomaster99;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.algomaster99.terminator.commons.fingerprint.Fingerprint;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Options {
    private List<Fingerprint> fingerprints;

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
                    fingerprints = parseFingerprints(new File(value));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown argument: " + key);
            }
        }
    }

    private static List<Fingerprint> parseFingerprints(File fingerprintsFile) {
        final ObjectMapper mapper = new ObjectMapper();
        try (MappingIterator<Fingerprint> it =
                mapper.readerFor(Fingerprint.class).readValues(fingerprintsFile)) {
            return it.readAll();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Fingerprint> getFingerprints() {
        if (fingerprints == null) {
            throw new IllegalStateException("Fingerprints not set");
        }
        return fingerprints;
    }
}
