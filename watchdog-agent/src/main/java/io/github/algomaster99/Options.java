package io.github.algomaster99;

import static io.github.algomaster99.terminator.commons.fingerprint.ParsingHelper.deserializeFingerprints;

import io.github.algomaster99.terminator.commons.fingerprint.provenance.Provenance;
import java.nio.file.Path;
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
                    fingerprints = deserializeFingerprints(Path.of(value));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown argument: " + key);
            }
        }
    }

    public Map<String, List<Provenance>> getFingerprints() {
        if (fingerprints == null) {
            throw new IllegalStateException("Fingerprints not set");
        }
        return fingerprints;
    }
}
