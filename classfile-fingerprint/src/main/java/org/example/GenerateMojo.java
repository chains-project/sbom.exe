package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.example.data.Fingerprint;

@Mojo(
        name = "generate",
        defaultPhase = LifecyclePhase.GENERATE_RESOURCES,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresOnline = true)
public class GenerateMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * The algorithm to use for computing the hash.
     *
     * @see <a href="https://docs.oracle.com/en/java/javase/17/docs/specs/security/standard-names.html#messagedigest-algorithms">Possible values</a>
     */
    @Parameter(defaultValue = "SHA256", required = true, property = "algorithm")
    private String algorithm;

    private final List<Fingerprint> fingerprints = new ArrayList<>();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Set<Artifact> resolvedArtifacts = project.getArtifacts();
        for (Artifact artifact : resolvedArtifacts) {
            getLog().info("Resolved artifact: " + artifact);
            processArtifact(artifact);
        }
        Path fingerprintFile = getFingerprintFile(project, algorithm);
        writeFingerprint(fingerprints, fingerprintFile);
        getLog().info("Wrote fingerprint to: " + fingerprintFile);
    }

    private void processArtifact(Artifact artifact) {
        getLog().debug("Processing artifact: " + artifact);
        File artifactFileOnSystem = artifact.getFile();
        getLog().debug("Artifact file on system: " + artifactFileOnSystem);

        try (JarFile jarFile = new JarFile(artifactFileOnSystem)) {
            Enumeration<JarEntry> jarEntries = jarFile.entries();
            while (jarEntries.hasMoreElements()) {
                JarEntry jarEntry = jarEntries.nextElement();
                if (jarEntry.getName().endsWith(".class")) {
                    getLog().debug("Found class: " + jarEntry.getName());
                    String hashOfClass = computeHash(jarFile.getInputStream(jarEntry), algorithm);

                    String jarEntryName =
                            jarEntry.getName().substring(0, jarEntry.getName().length() - ".class".length());

                    fingerprints.add(new Fingerprint(
                            artifact.getGroupId(),
                            artifact.getArtifactId(),
                            artifact.getVersion(),
                            jarEntryName,
                            hashOfClass));
                }
            }
        } catch (IOException e) {
            getLog().error("Could not open JAR file: " + artifactFileOnSystem);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeFingerprint(List<Fingerprint> fingerprints, Path fingerprintFile) {
        ObjectMapper mapper = new ObjectMapper();
        try (SequenceWriter seq =
                mapper.writer().withRootValueSeparator(System.lineSeparator()).writeValues(fingerprintFile.toFile())) {
            for (Fingerprint fingerprint : fingerprints) {
                seq.write(fingerprint);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String computeHash(InputStream byteStream, String algorithm) throws NoSuchAlgorithmException {
        try {
            byte[] bytes = byteStream.readAllBytes();
            byte[] algorithmSum = MessageDigest.getInstance(algorithm).digest(bytes);

            return toHexString(algorithmSum);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String toHexString(byte[] bytes) {
        Formatter result = new Formatter();
        try (result) {
            for (byte b : bytes) {
                result.format("%02x", b & 0xff);
            }
            return result.toString();
        }
    }

    private static Path getFingerprintFile(MavenProject project, String algorithm) {
        try {
            Files.createDirectories(Path.of(project.getBuild().getDirectory()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // see https://jsonlines.org/
        return Path.of(project.getBuild().getDirectory(), String.format("classfile.%s.jsonl", algorithm.toLowerCase()));
    }
}
