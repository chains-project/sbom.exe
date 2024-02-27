package io.github.algomaster99;

import static io.github.algomaster99.terminator.commons.fingerprint.ParsingHelper.deserializeFingerprints;

import io.github.algomaster99.terminator.commons.fingerprint.provenance.Provenance;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Options {

    private static final Logger LOGGER = LoggerFactory.getLogger(Options.class);
    private Map<String, Set<Provenance>> sbom;
    private boolean skipShutdown = false;
    private String algorithm = "SHA-256";

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
                case "sbom":
                    sbom = deserializeFingerprints(Path.of(value));
                    break;
                case "skipShutdown":
                    skipShutdown = Boolean.parseBoolean(value);
                    break;
                case "algorithm":
                    algorithm = value;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown argument: " + key);
            }
        }
    }

    public Map<String, Set<Provenance>> getSbom() {
        return sbom;
    }

    public void setSbomb(Map<String, Set<Provenance>> sbom) {
        this.sbom = sbom;
    }

    public boolean shouldSkipShutdown() {
        return skipShutdown;
    }
}
