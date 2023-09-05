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
        if (RuntimeClass.isProxyClass(classfileBuffer)) {
            return classfileBuffer;
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
                if (options.shouldSkipShutdown()) {
                    System.err.println("[MODIFIED]: " + className);
                    return classfileBuffer;
                } else {
                    blueScreenOfDeath("[MODIFIED]: " + className);
                    System.exit(1);
                    return null;
                }
            }
        }
        if (options.shouldSkipShutdown()) {
            System.err.println("[NOT WHITELISTED]: " + className);
            return classfileBuffer;
        } else {
            blueScreenOfDeath("[NOT WHITELISTED]: " + className);
            System.exit(1);
            return null;
        }
    }

    private static void blueScreenOfDeath(String classViolation) {
        final String WHITE = "\u001B[97m";
        final String BOLD = "\u001B[1m";
        final String BACKGROUND_LIGHT_BLUE = "\u001B[104m";

        String message = "                \n" + "             _  \n"
                + "           .' ) \n"
                + " ,.--.    / .'  \n"
                + "//    \\  / /    \n"
                + "\\\\    / / /     \n"
                + " `'--' . '      \n"
                + " ,.--. | |      \n"
                + "//   \\' '      \n"
                + "\\\\    / \\ \\     \n"
                + " `'--'   \\ \\    \n"
                + "          \\ '.  \n"
                + "           '._) \n"
                + "\n"
                + "\n"
                + "A fatal error has been detected by the Java Runtime Environment:\n"
                + "\n"
                + classViolation
                + "\n";

        System.out.println(BACKGROUND_LIGHT_BLUE + BOLD + WHITE);
        System.out.println(message);
    }
}
