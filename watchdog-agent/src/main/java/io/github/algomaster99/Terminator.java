package io.github.algomaster99;

import static io.github.algomaster99.terminator.commons.fingerprint.classfile.HashComputer.computeHash;

import io.github.algomaster99.terminator.commons.fingerprint.classfile.RuntimeClass;
import io.github.algomaster99.terminator.commons.fingerprint.provenance.Provenance;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.NoSuchAlgorithmException;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Terminator {

    private static final String NOT_WHITELISTED = "[NOT WHITELISTED]: ";
    private static final String MODIFIED = "[MODIFIED]: ";
    private static final Logger LOGGER = LoggerFactory.getLogger(Terminator.class);
    private static Options options;

    public static void premain(String agentArgs, Instrumentation inst) {
        options = new Options(agentArgs);
        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(
                    Module module,
                    ClassLoader loader,
                    String className,
                    Class<?> classBeingRedefined,
                    ProtectionDomain protectionDomain,
                    byte[] classfileBuffer) {
                if (classBeingRedefined.isLocalClass()
                        || classBeingRedefined.isAnonymousClass()
                        || classBeingRedefined.isMemberClass()) {
                    // TODO: we need hashes for inner classe
                    return classfileBuffer;
                }
                return isLoadedClassWhitelisted(className, classfileBuffer);
            }
        });
    }

    private static byte[] isLoadedClassWhitelisted(String className, byte[] classfileBuffer) {
        if (RuntimeClass.isProxyClass(classfileBuffer)
                || RuntimeClass.isGeneratedClassExtendingMagicAccessor(classfileBuffer)) {
            return classfileBuffer;
        }
        if (className.startsWith("java")) {
            // for testing
            return classfileBuffer;
        }
        if (options.getJdkFingerprints().containsKey(className)) {
            List<Provenance> candidates = options.getJdkFingerprints().get(className);
            for (Provenance candidate : candidates) {
                String hash = computeHashForProvance(candidate, classfileBuffer).orElse(null);
                if (hash == null) {
                    System.err.println("Error computing hash for " + className);
                    System.exit(1);
                    return null;
                }
                if (hash.equals(candidate.classFileAttributes().hash())) {
                    return classfileBuffer;
                }
            }
            System.err.println(MODIFIED + className);
            System.err.println("Fingerprint: " + options.getJdkFingerprints().get(className));
            if (options.shouldSkipShutdown()) {
                return classfileBuffer;
            } else {
                System.exit(1);
                return null;
            }
        }
        // now check if it is a dependency or application class
        if (options.getFingerprints().containsKey(className)) {
            List<Provenance> candidates = options.getJdkFingerprints().get(className);
            for (Provenance candidate : candidates) {
                if (className.equals("spoon/Launcher")) {
                    System.out.println("We found: " + candidates.size());
                }
                String hash = computeHashForProvance(candidate, classfileBuffer).orElse(null);
                if (hash == null) {
                    System.err.println("Error computing hash for " + className);
                    System.exit(1);
                    return null;
                }
                if (hash.equals(candidate.classFileAttributes().hash())) {
                    return classfileBuffer;
                }
            }
            System.err.println(MODIFIED + className);
            System.err.println("Fingerprint: " + options.getFingerprints().get(className));
            if (options.shouldSkipShutdown()) {
                return classfileBuffer;
            } else {
                System.exit(1);
                return null;
            }
        }
        System.err.println(NOT_WHITELISTED + className);
        if (options.shouldSkipShutdown()) {
            return classfileBuffer;
        } else {
            System.exit(1);
            return null;
        }
    }
    /**
     * Computes the hash of the classfile buffer using the algorithm specified in the provenance.
     * @param provenance  The provenance of the classfile.
     * @param classfileBuffer  The classfile buffer.
     * @return  The hash of the classfile buffer.
     */
    private static Optional<String> computeHashForProvance(Provenance provenance, byte[] classfileBuffer) {
        try {
            return Optional.ofNullable(computeHash(
                    classfileBuffer, provenance.classFileAttributes().algorithm()));
        } catch (NoSuchAlgorithmException e) {
            LOGGER.atDebug().log("No such algorithm: " + e.getMessage());
            return Optional.empty();
        }
    }
}
