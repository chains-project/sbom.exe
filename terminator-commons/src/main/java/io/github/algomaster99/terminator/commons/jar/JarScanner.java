package io.github.algomaster99.terminator.commons.jar;

import static io.github.algomaster99.terminator.commons.fingerprint.classfile.HashComputer.computeHash;

import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileAttributes;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileUtilities;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JarScanner {

    private JarScanner() {}

    public static void goInsideJarAndUpdateFingerprints(
            File artifactFileOnSystem,
            Map<String, Set<ClassFileAttributes>> fingerprints,
            String algorithm,
            String... provenanceInformation) {
        try (JarFile jarFile = new JarFile(artifactFileOnSystem)) {
            Enumeration<JarEntry> jarEntries = jarFile.entries();
            while (jarEntries.hasMoreElements()) {
                JarEntry jarEntry = jarEntries.nextElement();
                if (jarEntry.getName().endsWith(".class")) {
                    String jarEntryName =
                            jarEntry.getName().substring(0, jarEntry.getName().length() - ".class".length());
                    String strippedJarEntryName = stripMetaInf(jarEntryName);

                    byte[] classfileBytes = jarFile.getInputStream(jarEntry).readAllBytes();
                    String classfileVersion = ClassFileUtilities.getVersion(classfileBytes);
                    String hashOfClass = computeHash(classfileBytes);

                    ClassFileAttributes classFileAttributes =
                            new ClassFileAttributes(classfileVersion, hashOfClass, algorithm);

                    if (fingerprints.containsKey(strippedJarEntryName)) {
                        Set<ClassFileAttributes> alreadyExistingProvenance = fingerprints.get(strippedJarEntryName);
                        alreadyExistingProvenance.add(classFileAttributes);
                    } else {
                        Set<ClassFileAttributes> newProvenance = new HashSet<>();
                        newProvenance.add(classFileAttributes);
                        fingerprints.put(strippedJarEntryName, newProvenance);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not open JAR file: " + artifactFileOnSystem);
            throw new RuntimeException(e);
        }
    }

    /** Strips the META-INF/versions/{java_version}/ from the class name. */
    private static String stripMetaInf(String jarEntryName) {
        if (!jarEntryName.startsWith("META-INF")) {
            return jarEntryName;
        }
        final String regex = "META-INF\\/versions\\/[0-9]+\\/(.*)";
        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(jarEntryName);
        matcher.find();
        return matcher.group(1);
    }
}
