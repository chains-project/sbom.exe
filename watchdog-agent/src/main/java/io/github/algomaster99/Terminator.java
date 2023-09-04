package io.github.algomaster99;

import static io.github.algomaster99.terminator.commons.fingerprint.classfile.HashComputer.computeHash;

import io.github.algomaster99.terminator.commons.fingerprint.classfile.RuntimeClass;
import io.github.algomaster99.terminator.commons.fingerprint.provenance.Provenance;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.NoSuchAlgorithmException;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Map;

public class Terminator {
    private static Options options;

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
        if (RuntimeClass.isProxyClass(classfileBuffer)
                || RuntimeClass.isGeneratedClassExtendingMagicAccessor(classfileBuffer)
                || RuntimeClass.isUnsafeQualifiedStaticAccess(classfileBuffer)) {
            return classfileBuffer;
        }
        if (className.contains("$")) {
            System.err.println("[INNER CLASS]: " + className);
            // FIXME: we need to check inner classes without loading them. Maybe add the hashes for inner classes in the
            // fingerprints?
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
                System.err.println("Hash is different from the expected one");
                System.err.println("Expected: "
                        + candidates.stream()
                                .map(v -> v.classFileAttributes().hash())
                                .toList());
                try {
                    System.err.println("Actual: "
                            + computeHash(
                                    classfileBuffer,
                                    candidates.get(0).classFileAttributes().algorithm()));
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                if (options.shouldSkipShutdown()) {
                    return classfileBuffer;
                } else {
                    System.exit(1);
                    return null;
                }
            }
        }
        System.err.println("Unknown class: " + className);
        System.err.println("[NOT WHITELISTED]: " + className);
        if (options.shouldSkipShutdown()) {
            return classfileBuffer;
        } else {
            System.exit(1);
            return null;
        }
    }
}
