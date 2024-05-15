package io.github.algomaster99;

import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileAttributes;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileUtilities;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.HashComputer;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Terminator {
    private static Options options;

    private static Map<String, Set<ClassFileAttributes>> fingerprints;

    private static final Map<String, String> proxyOriginalNamesToReadableNames = new ConcurrentHashMap<>();

    public static void premain(String agentArgs, Instrumentation inst) {
        options = new Options(agentArgs);
        fingerprints = options.getSbom();

        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(
                    ClassLoader loader,
                    String className,
                    Class<?> classBeingRedefined,
                    ProtectionDomain protectionDomain,
                    byte[] classfileBuffer) {

                return isLoadedClassAllowlisted(className, classfileBuffer);
            }
        });
    }

    private static byte[] isLoadedClassAllowlisted(String className, byte[] classfileBuffer) {

        // this only works for Java 11 (atleast I have only tested for that)
        if (className.startsWith("com/sun/proxy/$Proxy") || className.startsWith("com/sun/proxy/jdk/")) {
            String nameThatNeedsToBeDisplayedInBomi =
                    "Proxy_" + ClassFileUtilities.getInterfacesOfProxyClass(classfileBuffer);
            proxyOriginalNamesToReadableNames.put(className, nameThatNeedsToBeDisplayedInBomi);
            return lookupReadableName(className, classfileBuffer, nameThatNeedsToBeDisplayedInBomi);
        }
        if (className.startsWith("jdk/internal/reflect/GeneratedConstructorAccessor")) {
            String classForWhichTheConstructorIs =
                    ClassFileUtilities.getClassForWhichGeneratedAccessorIsFor(classfileBuffer);
            String isProxy = proxyOriginalNamesToReadableNames.get(classForWhichTheConstructorIs);
            String actualGCAName;
            if (isProxy != null) {
                actualGCAName = "GCA_" + isProxy;
            } else {
                actualGCAName = "GCA_" + classForWhichTheConstructorIs;
            }
            return lookupReadableName(className, classfileBuffer, actualGCAName);
        }
        for (String expectedClassName : fingerprints.keySet()) {
            if (expectedClassName.equals(className)) {
                Set<ClassFileAttributes> candidates = fingerprints.get(expectedClassName);
                for (ClassFileAttributes candidate : candidates) {
                    String hash = HashComputer.computeHash(classfileBuffer);
                    if (hash.equals(candidate.hash())) {
                        return classfileBuffer;
                    }
                }
                return modified(className, classfileBuffer);
            }
        }
        return notAllowlisted(className, classfileBuffer);
    }

    private static byte[] lookupReadableName(String className, byte[] classfileBuffer, String correspondingClassName) {
        Set<ClassFileAttributes> attributes = fingerprints.get(correspondingClassName);
        if (attributes != null) {
            for (ClassFileAttributes attribute : attributes) {
                String hash = HashComputer.computeHash(classfileBuffer);
                if (hash.equals(attribute.hash())) {
                    return classfileBuffer;
                }
            }
            return modified(className, classfileBuffer);
        }
        return notAllowlisted(className, classfileBuffer);
    }

    private static byte[] modified(String className, byte[] classfileBuffer) {
        if (options.shouldSkipShutdown()) {
            System.err.println("[MODIFIED]: " + className);
            return classfileBuffer;
        } else {
            blueScreenOfDeath("[MODIFIED]: " + className);
            Runtime.getRuntime().halt(1);
            return null;
        }
    }

    private static byte[] notAllowlisted(String className, byte[] classfileBuffer) {
        if (options.shouldSkipShutdown()) {
            System.err.println("[NOT ALLOWLISTED]: " + className);
            return classfileBuffer;
        } else {
            blueScreenOfDeath("[NOT ALLOWLISTED]: " + className);
            Runtime.getRuntime().halt(1);
            return null;
        }
    }

    private static void blueScreenOfDeath(String classViolation) {
        final String WHITE = "\u001B[97m";
        final String BOLD = "\u001B[1m";
        final String BACKGROUND_LIGHT_BLUE = "\u001B[104m";
        final String RESET = "\u001B[0m";

        String message = "                \n" + "             _  \n"
                + "           .' ) \n"
                + " ,.--.    / .'  \n"
                + "//    \\  / /    \n"
                + "\\\\    / / /     \n"
                + " `'--' . '      \n"
                + " ,.--. | |      \n"
                + "//   \\' '       \n"
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
        System.out.println(RESET);
    }
}
