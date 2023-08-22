package io.github.algomaster99.options;

import io.github.algomaster99.terminator.commons.cyclonedx.Bom14Schema;
import io.github.algomaster99.terminator.commons.cyclonedx.CycloneDX;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FromSbomOptions {
    private final Bom14Schema input;
    private final String algorithm;
    private final Path output;
    private final Path externalJars;

    public FromSbomOptions(Path input, String algorithm, Path output, Path externalJars) throws IOException {
        this.algorithm = algorithm;
        this.input = CycloneDX.getPOJO(Files.readString(input));
        this.output = output;
        this.externalJars = externalJars;
    }

    public Bom14Schema getInput() {
        return input;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public Path getOutput() {
        return output;
    }

    public Path getExternalJars() {
        return externalJars;
    }
}
