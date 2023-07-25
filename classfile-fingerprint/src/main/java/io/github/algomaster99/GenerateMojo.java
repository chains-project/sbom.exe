package io.github.algomaster99;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;
import io.github.algomaster99.data.Fingerprint;
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
import java.util.stream.Stream;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

@Mojo(
        name = "generate",
        defaultPhase = LifecyclePhase.COMPILE,
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
        processProjectItself();
        processDependencies();

        Path fingerprintFile = getFingerprintFile(project, algorithm);
        writeFingerprint(fingerprints, fingerprintFile);
        getLog().info("Wrote fingerprint to: " + fingerprintFile);
    }

    private void processProjectItself() {
        getLog().info("Processing project itself");
        String groupId = project.getGroupId();
        String artifactId = project.getArtifactId();
        String version = project.getVersion();
        String projectPackaging = project.getPackaging();

        if ("pom".equals(projectPackaging)) {
            getLog().info(String.format(
                    "%s:%s:%s has pom packaging. It has no classes.", groupId, artifactId, version));
            return;
        }

        Path classesDirectory = Path.of(project.getBuild().getOutputDirectory());
        if (!classesDirectory.toFile().exists()) {
            getLog().info(String.format("%s:%s:%s does not have any classes", groupId, artifactId, version));
            getLog().warn("Make sure that you compiled the project before running this plugin!");
            getLog().warn("If you know you compiled the project, please ignore this warning.");
            return;
        }
        try (Stream<Path> stream = Files.walk(classesDirectory)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".class"))
                    .forEach(path -> {
                        String className = classesDirectory.relativize(path).toString();
                        className = className.substring(0, className.length() - ".class".length());
                        getLog().debug("Found class: " + className);
                        try (InputStream byteStream = Files.newInputStream(path)) {
                            String hashOfClass = computeHash(byteStream, algorithm);
                            fingerprints.add(new Fingerprint(groupId, artifactId, version, className, hashOfClass));
                        } catch (IOException e) {
                            getLog().error("Could not open file: " + path);
                        } catch (NoSuchAlgorithmException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        getLog().info(String.format(
                "Classes of %s:%s:%s:%s are included.", groupId, artifactId, version, projectPackaging));
    }

    private void processDependencies() {
        Set<Artifact> resolvedArtifacts = project.getArtifacts();
        for (Artifact artifact : resolvedArtifacts) {
            getLog().info("Resolved artifact: " + artifact);
            processDependency(artifact);
        }
    }

    private void processDependency(Artifact artifact) {
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
