package io.github.algomaster99;

import static io.github.algomaster99.terminator.commons.fingerprint.classfile.HashComputer.computeHash;
import static io.github.algomaster99.terminator.commons.jar.JarScanner.goInsideJarAndUpdateFingerprints;

import io.github.algomaster99.terminator.commons.cyclonedx.Bom14Schema;
import io.github.algomaster99.terminator.commons.cyclonedx.Component;
import io.github.algomaster99.terminator.commons.fingerprint.provenance.Provenance;
import io.github.algomaster99.terminator.commons.jar.JarDownloader;
import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.NoSuchAlgorithmException;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Map;

public class Terminator {
    private static Options options;

    private static final List<String> INTERNAL_PACKAGES =
            List.of("java/", "javax/", "jdk/", "sun/", "com/sun/", "org/xml/sax", "org/w3c/dom/");

    public static void premain(String agentArgs, Instrumentation inst) {
        options = new Options(agentArgs);
        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(
                    ClassLoader loader,
                    String className,
                    Class<?> classBeingRedefined,
                    ProtectionDomain protectionDomain,
                    byte[] classfileBuffer) {
                return isLoadedClassWhitelisted(className, classfileBuffer);
            }
        });
    }

    private static byte[] isLoadedClassWhitelisted(String className, byte[] classfileBuffer) {
        Map<String, List<Provenance>> fingerprints = options.getFingerprints();
        Bom14Schema sbom = options.getSbom();
        // Block needed to consider the project's own classes
        // Assumes that package is released on maven central
        if (sbom != null) {
            try {
                Component rootComponent = sbom.getMetadata().getComponent();
                File jarFile = JarDownloader.getMavenJarFile(
                        rootComponent.getGroup(), rootComponent.getName(), rootComponent.getVersion());
                goInsideJarAndUpdateFingerprints(
                        jarFile,
                        fingerprints,
                        // TODO: Make this configurable
                        "SHA256",
                        rootComponent.getGroup(),
                        rootComponent.getName(),
                        rootComponent.getVersion());
            } catch (IOException e) {
                System.err.println("Failed to download jar file: " + e.getMessage());
                System.exit(1);
            } catch (InterruptedException e) {
                System.err.println("Downloading was interrupted: " + e.getMessage());
                System.exit(1);
            }
        }
        if (INTERNAL_PACKAGES.stream().anyMatch(className::startsWith)) {
            return classfileBuffer;
        }
        for (String expectedClassName : fingerprints.keySet()) {
            if (expectedClassName.equals(className)) {
                List<Provenance> candidates = fingerprints.get(expectedClassName);
                for (Provenance candidate : candidates) {
                    String hash;
                    try {
                        hash = computeHash(
                                classfileBuffer, candidate.classFileAttributes().algorithm());
                    } catch (NoSuchAlgorithmException e) {
                        System.err.println("No such algorithm: " + e.getMessage());
                        System.exit(1);
                        return null;
                    }
                    if (hash.equals(candidate.classFileAttributes().hash())) {
                        return classfileBuffer;
                    }
                }
                System.err.println("[MODIFIED]: " + className);
                if (options.shouldSkipShutdown()) {
                    return classfileBuffer;
                } else {
                    System.exit(1);
                    return null;
                }
            }
        }
        System.err.println("[NOT WHITELISTED]: " + className);
        if (options.shouldSkipShutdown()) {
            return classfileBuffer;
        } else {
            System.exit(1);
            return null;
        }
    }
}
