package io.github.algomaster99;

import static io.github.algomaster99.terminator.commons.fingerprint.ParsingHelper.deserializeFingerprints;

import io.github.algomaster99.terminator.commons.fingerprint.protobuf.Bomi;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Options {

    private static final Logger LOGGER = LoggerFactory.getLogger(Options.class);
    private Bomi sbom;
    private boolean skipShutdown = false;

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
                default:
                    throw new IllegalArgumentException("Unknown argument: " + key);
            }
        }
    }

    public Bomi getSbom() {
        return sbom;
    }

    public boolean shouldSkipShutdown() {
        return skipShutdown;
    }
}
