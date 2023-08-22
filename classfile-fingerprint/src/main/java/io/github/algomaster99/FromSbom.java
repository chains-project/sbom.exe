package io.github.algomaster99;

import static io.github.algomaster99.terminator.commons.jar.JarScanner.goInsideJarAndUpdateFingerprints;
import static io.github.algomaster99.terminator.commons.jar.JarScanner.processExternalJars;

import io.github.algomaster99.options.FromSbomOptions;
import io.github.algomaster99.terminator.commons.cyclonedx.Bom14Schema;
import io.github.algomaster99.terminator.commons.cyclonedx.Component;
import io.github.algomaster99.terminator.commons.fingerprint.ParsingHelper;
import io.github.algomaster99.terminator.commons.fingerprint.provenance.Provenance;
import io.github.algomaster99.terminator.commons.jar.JarDownloader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

@CommandLine.Command(
        name = "from-sbom",
        mixinStandardHelpOptions = true,
        description = "Converts a CycloneDX SBOM to a fingerprint file.")
public class FromSbom implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(FromSbom.class);

    @CommandLine.Option(
            names = {"-i", "--input"},
            required = true,
            description = "Input SBOM file.")
    private Path input;

    /**
     * The algorithm to use for computing the hash.
     *
     * @see <a href="https://docs.oracle.com/en/java/javase/17/docs/specs/security/standard-names.html#messagedigest-algorithms">Possible values</a>
     */
    @CommandLine.Option(
            names = {"-a", "--algorithm"},
            required = false,
            description = "The algorithm to use for computing the hash.")
    private String algorithm = "SHA256";

    @CommandLine.Option(
            names = {"-o", "--output"},
            required = false,
            description = "The output file.")
    private Path output = Path.of(String.format("classfile.%s.json", algorithm.toLowerCase()));

    @CommandLine.Option(
            names = {"-e", "--external-jars"},
            required = false,
            description = "Path to known external jars.")
    private Path externalJars;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new FromSbom()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        try {
            FromSbomOptions options = new FromSbomOptions(input, algorithm, output, externalJars);
            Map<String, List<Provenance>> fingerprints = getFingerprints(options);
            ParsingHelper.serialiseFingerprints(fingerprints, options.getOutput());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, List<Provenance>> getFingerprints(FromSbomOptions options) {
        Bom14Schema sbom = options.getInput();
        Map<String, List<Provenance>> fingerprints = new HashMap<>();
        processExternalJars(options.getExternalJars().toFile(), fingerprints, options.getAlgorithm());

        for (Component component : sbom.getComponents()) {
            try {
                File jarFile = JarDownloader.getMavenJarFile(
                        component.getGroup(), component.getName(), component.getVersion());
                goInsideJarAndUpdateFingerprints(
                        jarFile,
                        fingerprints,
                        options.getAlgorithm(),
                        component.getGroup(),
                        component.getName(),
                        component.getVersion());
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return fingerprints;
    }
}
