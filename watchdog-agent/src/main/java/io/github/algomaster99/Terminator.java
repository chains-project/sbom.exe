package io.github.algomaster99;

import static io.github.algomaster99.terminator.commons.HashComputer.computeHash;

import io.github.algomaster99.terminator.commons.fingerprint.Fingerprint;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.NoSuchAlgorithmException;
import java.security.ProtectionDomain;
import java.util.List;

public class Terminator {
    private static Options options;

    private static final List<String> INTERNAL_PACKAGES =
            List.of("java/", "javax/", "jdk/", "sun/", "com/sun/", "org/xml/sax");

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
                    hash = computeHash(classfileBuffer, fingerprint.algorithm());
                } catch (NoSuchAlgorithmException e) {
                    System.err.println("No such algorithm: " + e.getMessage());
                    System.exit(1);
                    return null;
                }
                if (hash.equals(fingerprint.hash())) {
                    return classfileBuffer;
                } else {
                    System.err.println("[MODIFIED]: " + className);
                    System.exit(1);
                    return null;
                }
            }
        }
        System.err.println("[NOT WHITELISTED]: " + className);
        System.exit(1);
        return null;
    }
}
