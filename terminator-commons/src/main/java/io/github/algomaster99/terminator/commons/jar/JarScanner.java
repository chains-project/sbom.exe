package io.github.algomaster99.terminator.commons.jar;

import static io.github.algomaster99.terminator.commons.fingerprint.classfile.HashComputer.computeHash;

import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassfileVersion;
import io.github.algomaster99.terminator.commons.fingerprint.protobuf.Bomi;
import io.github.algomaster99.terminator.commons.fingerprint.protobuf.BomiUtility;
import io.github.algomaster99.terminator.commons.fingerprint.protobuf.ClassFile;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JarScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(JarScanner.class);

    private JarScanner() {}

    public static void goInsideJarAndUpdateFingerprints(File artifactFileOnSystem, Bomi.Builder bomiBuilder) {
        try (JarFile jarFile = new JarFile(artifactFileOnSystem)) {
            Enumeration<JarEntry> jarEntries = jarFile.entries();
            while (jarEntries.hasMoreElements()) {
                JarEntry jarEntry = jarEntries.nextElement();
                if (jarEntry.getName().endsWith(".class")) {
                    LOGGER.debug("Found class: " + jarEntry.getName());

                    String jarEntryName =
                            jarEntry.getName().substring(0, jarEntry.getName().length() - ".class".length());
                    String strippedJarEntryName = stripMetaInf(jarEntryName);

                    byte[] classfileBytes = jarFile.getInputStream(jarEntry).readAllBytes();
                    String classfileVersion = ClassfileVersion.getVersion(classfileBytes);
                    String hashOfClass = computeHash(classfileBytes);

                    Optional<ClassFile> classFileCandidate =
                            BomiUtility.isClassFilePresent(bomiBuilder, strippedJarEntryName);

                    if (classFileCandidate.isPresent()) {
                        ClassFile classFile = classFileCandidate.get();

                        if (BomiUtility.isHashSame(classFile, hashOfClass)) {
                            continue;
                        }

                        int indexOfClassFile = bomiBuilder.getClassFileList().indexOf(classFile);

                        ClassFile.Builder classFileBuilder = ClassFile.newBuilder();
                        classFileBuilder.mergeFrom(classFile);

                        classFileBuilder.addAttribute(ClassFile.Attribute.newBuilder()
                                .setVersion(classfileVersion)
                                .setHash(hashOfClass)
                                .build());
                        bomiBuilder.removeClassFile(indexOfClassFile); // remove the old one
                        bomiBuilder.addClassFile(indexOfClassFile, classFileBuilder.build());
                    } else {
                        ClassFile classFile = ClassFile.newBuilder()
                                .setClassName(strippedJarEntryName)
                                .addAttribute(ClassFile.Attribute.newBuilder()
                                        .setVersion(classfileVersion)
                                        .setHash(hashOfClass)
                                        .build())
                                .build();
                        bomiBuilder.addClassFile(classFile);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Could not open JAR file: " + artifactFileOnSystem);
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
