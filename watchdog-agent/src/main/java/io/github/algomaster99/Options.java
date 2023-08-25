package io.github.algomaster99;

import static io.github.algomaster99.terminator.commons.fingerprint.ParsingHelper.deserializeFingerprints;
import static io.github.algomaster99.terminator.commons.jar.JarScanner.goInsideJarAndUpdateFingerprints;
import static io.github.algomaster99.terminator.commons.jar.JarScanner.processExternalJars;

import io.github.algomaster99.terminator.commons.cyclonedx.Bom14Schema;
import io.github.algomaster99.terminator.commons.cyclonedx.Component;
import io.github.algomaster99.terminator.commons.cyclonedx.CycloneDX;
import io.github.algomaster99.terminator.commons.fingerprint.JdkIndexer;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileAttributes;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassfileVersion;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.HashComputer;
import io.github.algomaster99.terminator.commons.fingerprint.provenance.Jdk;
import io.github.algomaster99.terminator.commons.fingerprint.provenance.Provenance;
import io.github.algomaster99.terminator.commons.jar.JarDownloader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.bytebuddy.jar.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Options {

    private static final Logger LOGGER = LoggerFactory.getLogger(Options.class);
    private Map<String, List<Provenance>> fingerprints = new HashMap<>();
    private Map<String, List<Provenance>> jdkFingerprints = new HashMap<>();
    private boolean skipShutdown = false;

    private boolean isSbomPassed = false;

    private String algorithm = "SHA-256";

    private Path output = Path.of(String.format("classfile.%s.json", algorithm.toLowerCase()));

    private Path externalJars;

    public Options(String agentArgs) {
        String[] args = agentArgs.split(",", -1);
        Path sbomPath = null;
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
                    // If an SBOM is passed included the root component in the fingerprints
                    sbomPath = Path.of(value);
                    isSbomPassed = true;
                    break;
                case "algorithm":
                    algorithm = value;
                    break;
                case "output":
                    output = Path.of(value);
                    break;
                case "externalJars":
                    externalJars = Path.of(value);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown argument: " + key);
            }
        }
        if (isSbomPassed) {
            LOGGER.info("Generating fingerprint from SBOM and external jars");
            try {
                Bom14Schema sbom = CycloneDX.getPOJO(Files.readString(sbomPath));
                LOGGER.debug("Processing root component: " + sbom.getMetadata().getComponent());
                processRootComponent(sbom);
                LOGGER.debug("Processing all components");
                processAllComponents(sbom);
                if (externalJars != null) {
                    LOGGER.debug("Processing external jars");
                    processExternalJars(externalJars.toFile(), fingerprints, algorithm);
                }
            } catch (InterruptedException e) {
                System.err.println("Downloading jars was interrupted: " + e.getMessage());
                System.exit(1);
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to read sbom file: " + sbomPath);
            }
        } else {
            LOGGER.info("Taking fingerprint from file: " + fingerprints);
        }
        processJdk();
    }

    public Map<String, List<Provenance>> getFingerprints() {
        return fingerprints;
    }

    public Map<String, List<Provenance>> getJdkFingerprints() {
        return jdkFingerprints;
    }

    public boolean shouldSkipShutdown() {
        return skipShutdown;
    }

    public boolean isSbomPassed() {
        return isSbomPassed;
    }

    public Path getOutput() {
        return output;
    }

    private void processRootComponent(Bom14Schema sbom) throws IOException, InterruptedException {
        Component rootComponent = sbom.getMetadata().getComponent();
        File jarFile = JarDownloader.getMavenJarFile(
                rootComponent.getGroup(), rootComponent.getName(), rootComponent.getVersion());
        goInsideJarAndUpdateFingerprints(
                jarFile,
                fingerprints,
                algorithm,
                rootComponent.getGroup(),
                rootComponent.getName(),
                rootComponent.getVersion());
    }

    private void processAllComponents(Bom14Schema sbom) {
        for (Component component : sbom.getComponents()) {
            try {
                File jarFile = JarDownloader.getMavenJarFile(
                        component.getGroup(), component.getName(), component.getVersion());
                goInsideJarAndUpdateFingerprints(
                        jarFile,
                        fingerprints,
                        algorithm,
                        component.getGroup(),
                        component.getName(),
                        component.getVersion());
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void processJdk() {
        JdkIndexer.listJdkClasses().forEach(resource -> {
            try {

                byte[] classfileBytes = resource.load();
                ClassReader classReader = new ClassReader(classfileBytes);

                String classfileVersion = ClassfileVersion.getVersion(classfileBytes);
                String hash = HashComputer.computeHash(classfileBytes, algorithm);
                jdkFingerprints.put(
                        classReader.getClassName(),
                        List.of(new Jdk(new ClassFileAttributes(classfileVersion, hash, algorithm))));
            } catch (IOException e) {
                LOGGER.error("Failed to load classfile bytes for: " + resource, e);
                throw new RuntimeException(e);
            } catch (NoSuchAlgorithmException e) {
                LOGGER.error("Failed to compute hash with algorithm: " + algorithm, e);
                throw new RuntimeException(e);
            } catch (Exception e) {
                LOGGER.error("Failed to compute hash for: " + resource, e);
                System.out.println("Failed to compute hash for: " + resource.getPath());
            }
        });
    }
}
