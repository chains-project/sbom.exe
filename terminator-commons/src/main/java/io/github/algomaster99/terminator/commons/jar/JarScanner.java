package io.github.algomaster99.terminator.commons.jar;

import static io.github.algomaster99.terminator.commons.fingerprint.classfile.HashComputer.computeHash;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.algomaster99.terminator.commons.data.ExternalJar;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileAttributes;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassfileVersion;
import io.github.algomaster99.terminator.commons.fingerprint.provenance.Jar;
import io.github.algomaster99.terminator.commons.fingerprint.provenance.Maven;
import io.github.algomaster99.terminator.commons.fingerprint.provenance.Provenance;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JarScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(JarScanner.class);

    private JarScanner() {}

    public static void goInsideJarAndUpdateFingerprints(
            File artifactFileOnSystem,
            Map<String, List<Provenance>> fingerprints,
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
                        List<Provenance> alreadyExistingProvenance = fingerprints.get(jarEntryName);
                        // TODO: should be removed after https://github.com/ASSERT-KTH/sbom.exe/issues/96 is fixed
                        if (alreadyExistingProvenance.stream()
                                .anyMatch(provenance ->
                                        provenance.classFileAttributes().hash().equals(hashOfClass))) {
                            continue;
                        }
                        updateProvenanceList(alreadyExistingProvenance, classFileAttributes, provenanceInformation);
                    } else {
                        List<Provenance> newProvenance = new ArrayList<>();
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

    public static void processExternalJars(
            File externalJars, Map<String, List<Provenance>> fingerprints, String algorithm) {
        if (externalJars == null) {
            LOGGER.info("No external jars are known.");
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        List<ExternalJar> externalJarList;
        try {
            InjectableValues inject = new InjectableValues.Std().addValue("configFile", externalJars.getAbsolutePath());
            externalJarList = mapper.setInjectableValues(inject)
                    .readerFor(new TypeReference<List<ExternalJar>>() {})
                    .readValue(externalJars);
        } catch (IOException e) {
            throw new RuntimeException("Could not open external jar file: " + e);
        }

        for (ExternalJar jar : externalJarList) {
            LOGGER.info("Processing external jar" + jar.path().getAbsolutePath());
            goInsideJarAndUpdateFingerprints(
                    jar.path().getAbsoluteFile(),
                    fingerprints,
                    algorithm,
                    jar.path().getAbsolutePath());
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
}
