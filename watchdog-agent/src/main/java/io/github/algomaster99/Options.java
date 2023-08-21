package io.github.algomaster99;

import static io.github.algomaster99.terminator.commons.fingerprint.ParsingHelper.deserializeFingerprints;

import io.github.algomaster99.terminator.commons.cyclonedx.Bom14Schema;
import io.github.algomaster99.terminator.commons.cyclonedx.CycloneDX;
import io.github.algomaster99.terminator.commons.fingerprint.provenance.Provenance;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class Options {
    private Map<String, List<Provenance>> fingerprints;

    private boolean skipShutdown = false;

    private Bom14Schema sbom = null;

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
                case "skipShutdown":
                    skipShutdown = Boolean.parseBoolean(value);
                    break;
                case "sbom":
                    Path sbomPath = Path.of(value);
                    try {
                        sbom = CycloneDX.getPOJO(Files.readString(sbomPath));
                    } catch (IOException e) {
                        throw new IllegalArgumentException("Failed to read sbom file: " + value);
                    }
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

    public boolean shouldSkipShutdown() {
        return skipShutdown;
    }

    public Bom14Schema getSbom() {
        return sbom;
    }
}
