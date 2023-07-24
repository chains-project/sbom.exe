package io.github.algomaster99;

import io.github.algomaster99.data.Fingerprint;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.ProtectionDomain;
import java.util.Formatter;
import java.util.List;

public class Terminator {
    private static Options options;

    private static final List<String> INTERNAL_PACKAGES = List.of("java/", "javax/", "jdk/", "sun/");

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
        List<Fingerprint> fingerprints = options.getFingerprints();
        if (INTERNAL_PACKAGES.stream().anyMatch(className::startsWith)) {
            return classfileBuffer;
        }
        for (Fingerprint fingerprint : fingerprints) {
            if (className.equals(fingerprint.className())) {
                String hash;
                try {
                    hash = computeHash(classfileBuffer);
                } catch (NoSuchAlgorithmException e) {
                    System.err.println("No such algorithm: " + e.getMessage());
                    System.exit(1);
                    return null;
                }
                if (hash.equals(fingerprint.hash())) {
                    return classfileBuffer;
                } else {
                    System.err.println("Class " + className + " has been modified");
                    System.exit(1);
                    return null;
                }
            }
        }
        System.err.println("Class " + className + " is not whitelisted");
        System.exit(1);
        return null;
    }

    // TODO: Duplicate of the method from GenerateMojo class
    private static String computeHash(byte[] bytes) throws NoSuchAlgorithmException {
        // TODO: softcode the hash algorithm
        byte[] algorithmSum = MessageDigest.getInstance("SHA256").digest(bytes);
        return toHexString(algorithmSum);
    }

    // TODO: Duplicate of the method from GenerateMojo class
    private static String toHexString(byte[] bytes) {
        Formatter result = new Formatter();
        try (result) {
            for (byte b : bytes) {
                result.format("%02x", b & 0xff);
            }
            return result.toString();
        }
    }
}
