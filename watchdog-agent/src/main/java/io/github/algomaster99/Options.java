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
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        String[] args = agentArgs.split(",");
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
                LOGGER.debug("Processing root component");
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
        if (rootComponent == null) {
            LOGGER.warn("Root component is not present.");
            return;
        }
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
                if (jarFile == null) {
                    LOGGER.warn(
                            "Could not find jar for {}:{}:{}",
                            component.getGroup(),
                            component.getName(),
                            component.getVersion());
                    continue;
                }
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
                byte[] classfileBytes = toArray(resource.bytes());
                String classfileVersion = ClassfileVersion.getVersion(classfileBytes);
                String hash = HashComputer.computeHash(classfileBytes, algorithm);
                jdkFingerprints.computeIfAbsent(
                        resource.name(),
                        k -> new ArrayList<>(
                                List.of((new Jdk(new ClassFileAttributes(classfileVersion, hash, algorithm))))));
                jdkFingerprints.computeIfPresent(resource.name(), (k, v) -> {
                    v.add(new Jdk(new ClassFileAttributes(classfileVersion, hash, algorithm)));
                    return v;
                });
                fingerprints.computeIfAbsent(
                        resource.name(),
                        k -> new ArrayList<>(
                                List.of((new Jdk(new ClassFileAttributes(classfileVersion, hash, algorithm))))));
                fingerprints.computeIfPresent(resource.name(), (k, v) -> {
                    v.add(new Jdk(new ClassFileAttributes(classfileVersion, hash, algorithm)));
                    return v;
                });
            } catch (NoSuchAlgorithmException e) {
                LOGGER.error("Failed to compute hash with algorithm: " + algorithm, e);
                throw new RuntimeException(e);
            } catch (Exception e) {
                LOGGER.error("Failed to compute hash for: " + resource, e);
            }
        });
    }

    /**
     * Converts a bytebuffer to a byte array. If the buffer has an array, it returns it, otherwise it copies the bytes. This is needed because the buffer is not guaranteed to have an array.
     * See {@link java.nio.ByteBuffer#hasArray()} and {@link java.nio.DirectByteBuffer}.
     * @param buffer  the buffer to convert
     * @return  the byte array
     */
    private byte[] toArray(ByteBuffer buffer) {
        if (buffer.hasArray()) {
            return buffer.array();
        }
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }
}
