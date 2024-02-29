package io.github.algomaster99.terminator.commons.jar;

import static io.github.algomaster99.terminator.commons.fingerprint.classfile.HashComputer.computeHash;

import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileAttributes;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassfileVersion;
import io.github.algomaster99.terminator.commons.fingerprint.provenance.Jar;
import io.github.algomaster99.terminator.commons.fingerprint.provenance.Maven;
import io.github.algomaster99.terminator.commons.fingerprint.provenance.Provenance;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JarScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(JarScanner.class);

    private JarScanner() {}

    public static void goInsideJarAndUpdateFingerprints(
            File artifactFileOnSystem,
            Map<String, Set<Provenance>> fingerprints,
            String algorithm,
            String... provenanceInformation) {
        try (JarFile jarFile = new JarFile(artifactFileOnSystem)) {
            Enumeration<JarEntry> jarEntries = jarFile.entries();
            while (jarEntries.hasMoreElements()) {
                JarEntry jarEntry = jarEntries.nextElement();
                if (jarEntry.getName().endsWith(".class")) {
                    LOGGER.debug("Found class: " + jarEntry.getName());

                    String jarEntryName =
                            jarEntry.getName().substring(0, jarEntry.getName().length() - ".class".length());
                    byte[] classfileBytes = jarFile.getInputStream(jarEntry).readAllBytes();
                    String classfileVersion = ClassfileVersion.getVersion(classfileBytes);
                    String hashOfClass = computeHash(classfileBytes, algorithm);

                    ClassFileAttributes classFileAttributes =
                            new ClassFileAttributes(classfileVersion, hashOfClass, algorithm);

                    if (fingerprints.containsKey(jarEntryName)) {
                        Set<Provenance> alreadyExistingProvenance = fingerprints.get(jarEntryName);
                        updateProvenanceList(alreadyExistingProvenance, classFileAttributes, provenanceInformation);
                    } else {
                        Set<Provenance> newProvenance = new HashSet<>();
                        updateProvenanceList(newProvenance, classFileAttributes, provenanceInformation);
                        fingerprints.put(jarEntryName, newProvenance);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Could not open JAR file: " + artifactFileOnSystem);
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static void updateProvenanceList(
            Set<Provenance> provenances, ClassFileAttributes classFileAttributes, String... provenanceInformation) {
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
}
