package io.github.algomaster99;

import static io.github.algomaster99.terminator.commons.HashComputer.computeHash;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;
import io.github.algomaster99.terminator.commons.ClassfileVersion;
import io.github.algomaster99.terminator.commons.data.ExternalJar;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileAttributes;
import io.github.algomaster99.terminator.commons.fingerprint.provenance.Jar;
import io.github.algomaster99.terminator.commons.fingerprint.provenance.Maven;
import io.github.algomaster99.terminator.commons.fingerprint.provenance.Provenance;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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

    /**
     * Path to known external jars.
     */
    @Parameter(property = "externalJars")
    private File externalJars;

    private final Map<String, List<Provenance>> fingerprints = new HashMap<>();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        processProjectItself();
        processDependencies();
        processExternalJars();

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
        walkOverClassDirectory(classesDirectory.toFile(), groupId, artifactId, version);
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

        if (artifactFileOnSystem.toString().endsWith(".jar")) {
            goInsideJar(artifactFileOnSystem, artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
        } else {
            getLog().debug("Artifact is not a JAR file: " + artifactFileOnSystem);
            getLog().debug("Artifact might be in classes directory.");
            walkOverClassDirectory(
                    artifactFileOnSystem, artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
        }
    }

    private void goInsideJar(File artifactFileOnSystem, String... provenanceInformation) {
        try (JarFile jarFile = new JarFile(artifactFileOnSystem)) {
            Enumeration<JarEntry> jarEntries = jarFile.entries();
            while (jarEntries.hasMoreElements()) {
                JarEntry jarEntry = jarEntries.nextElement();
                if (jarEntry.getName().endsWith(".class")) {
                    getLog().debug("Found class: " + jarEntry.getName());

                    String jarEntryName =
                            jarEntry.getName().substring(0, jarEntry.getName().length() - ".class".length());
                    byte[] classfileBytes = jarFile.getInputStream(jarEntry).readAllBytes();
                    String classfileVersion = ClassfileVersion.getVersion(classfileBytes);
                    String hashOfClass = computeHash(classfileBytes, algorithm);

                    ClassFileAttributes classFileAttributes =
                            new ClassFileAttributes(classfileVersion, hashOfClass, algorithm);

                    if (fingerprints.containsKey(jarEntryName)) {
                        List<Provenance> alreadyExistingProvenance = fingerprints.get(jarEntryName);
                        updateProvenanceList(alreadyExistingProvenance, classFileAttributes, provenanceInformation);
                    } else {
                        List<Provenance> newProvenance = new ArrayList<>();
                        updateProvenanceList(newProvenance, classFileAttributes, provenanceInformation);
                        fingerprints.put(jarEntryName, newProvenance);
                    }
                }
            }
        } catch (IOException e) {
            getLog().error("Could not open JAR file: " + artifactFileOnSystem);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static void updateProvenanceList(
            List<Provenance> provenances, ClassFileAttributes classFileAttributes, String... provenanceInformation) {
        if (provenanceInformation.length == 3) {
            String groupId = provenanceInformation[0];
            String artifactId = provenanceInformation[1];
            String version = provenanceInformation[2];

            provenances.add(new Maven(classFileAttributes, groupId, artifactId, version));
        } else if (provenanceInformation.length == 1) {
            String jarLocation = provenanceInformation[0];
            provenances.add(new Jar(classFileAttributes, jarLocation));
        } else {
            throw new RuntimeException("Wrong number of elements in provenance information.");
        }
    }

    private void walkOverClassDirectory(File artifactFileOnSystem, String groupId, String artifactId, String version) {
        try (Stream<Path> stream = Files.walk(artifactFileOnSystem.toPath())) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".class"))
                    .forEach(path -> {
                        String className =
                                artifactFileOnSystem.toPath().relativize(path).toString();
                        className = className
                                .substring(0, className.length() - ".class".length())
                                // Windows path contain "\\" as delimiters
                                .replace("\\", "/");
                        getLog().debug("Found class: " + className);
                        try (InputStream byteStream = Files.newInputStream(path)) {
                            byte[] classfileBytes = byteStream.readAllBytes();
                            String hashOfClass = computeHash(classfileBytes, algorithm);
                            String classfileVersion = ClassfileVersion.getVersion(classfileBytes);

                            ClassFileAttributes classFileAttributes =
                                    new ClassFileAttributes(classfileVersion, hashOfClass, algorithm);
                            Provenance provenance = new Maven(classFileAttributes, groupId, artifactId, version);

                            if (fingerprints.containsKey(className)) {
                                List<Provenance> alreadyExistingProvenance = fingerprints.get(className);
                                alreadyExistingProvenance.add(provenance);
                            } else {
                                List<Provenance> newProvenance = new ArrayList<>();
                                newProvenance.add(provenance);
                                fingerprints.put(className, newProvenance);
                            }
                        } catch (IOException e) {
                            getLog().error("Could not open file: " + path);
                        } catch (NoSuchAlgorithmException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (IOException e) {
            getLog().error("Could not open target/classes directory as well: " + artifactFileOnSystem);
            throw new RuntimeException(e);
        }
    }

    private void processExternalJars() {
        if (externalJars == null) {
            getLog().info("No external jars are known.");
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        List<ExternalJar> externalJarList;
        try {
            externalJarList =
                    mapper.readerFor(new TypeReference<List<ExternalJar>>() {}).readValue(externalJars);
        } catch (IOException e) {
            throw new RuntimeException("Could not open external jar file: " + e);
        }

        for (ExternalJar jar : externalJarList) {
            getLog().info("Processing external jar" + jar.path().getAbsolutePath());
            goInsideJar(jar.path().getAbsoluteFile(), jar.path().getAbsolutePath());
        }
    }

    private static void writeFingerprint(Map<String, List<Provenance>> fingerprints, Path fingerprintFile) {
        ObjectMapper mapper = new ObjectMapper();
        try (SequenceWriter seq =
                mapper.writer().withRootValueSeparator(System.lineSeparator()).writeValues(fingerprintFile.toFile())) {
            for (var fingerprint : fingerprints.entrySet()) {
                seq.write(fingerprint);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
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
