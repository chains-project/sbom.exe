package io.github.algomaster99;

import io.github.algomaster99.terminator.commons.fingerprint.JdkDigest;
import io.github.algomaster99.terminator.commons.fingerprint.JdkImage;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileAttributes;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileUtilities;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.HashComputer;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ProxyRegenerator;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.SpeciesRegenerator;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Terminator {
    private static Options options;

    private static Map<String, Set<ClassFileAttributes>> fingerprints;

    private static final Map<String, String> proxyOriginalNamesToReadableNames = new ConcurrentHashMap<>();

    // Set once the running JDK matches a digest in the BOMI.
    private static boolean jdkAllowlisted;

    public static void premain(String agentArgs, Instrumentation inst) {
        options = new Options(agentArgs);
        fingerprints = options.getSbom();
        // Many JDK classes, most of java.base, are loaded before the transformer is added and never reach it. The
        // digest covers them only if they came from the JDK's files: classes that --patch-module or
        // -Xbootclasspath/a loaded before this point are missed.
        verifyJdk();
        ProxyRegenerator.openGenerator(inst);
        SpeciesRegenerator.openGenerator(inst);

        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(
                    Module module,
                    ClassLoader loader,
                    String className,
                    Class<?> classBeingRedefined,
                    ProtectionDomain protectionDomain,
                    byte[] classfileBuffer) {

                return isLoadedClassAllowlisted(module, loader, className, classfileBuffer);
            }
        });
    }

    /**
     * Checks the running JDK against the JDK digests in the BOMI. A BOMI without digests keeps the hash of each JDK
     * class instead, so there is nothing to check here.
     */
    private static void verifyJdk() {
        Set<ClassFileAttributes> allowedJdks = fingerprints.get(JdkDigest.BOMI_KEY);
        if (allowedJdks == null) {
            return;
        }
        Path javaHome = Path.of(System.getProperty("java.home"));
        Map<String, String> digestByAlgorithm = new HashMap<>();
        for (ClassFileAttributes allowedJdk : allowedJdks) {
            String digest = digestByAlgorithm.computeIfAbsent(
                    allowedJdk.algorithm(), algorithm -> JdkDigest.compute(javaHome, algorithm));
            if (digest.equals(allowedJdk.hash())) {
                JdkImage.preload();
                jdkAllowlisted = true;
                System.err.println("[ALLOWLISTED]: JDK " + allowedJdk.classfileVersion() + " at " + javaHome);
                return;
            }
        }
        notAllowlisted("JDK " + JdkDigest.version(javaHome) + " at " + javaHome, null);
    }

    private static byte[] isLoadedClassAllowlisted(
            Module module, ClassLoader loader, String className, byte[] classfileBuffer) {
        // Proxy classes are not in the BOMI because their bytecode depends on the JDK. A class that only claims to be
        // a proxy is not regenerated identically, so it falls through to the lookup below.
        if (ClassFileUtilities.isProxyClass(classfileBuffer)
                && ProxyRegenerator.isGeneratedByJdk(loader, classfileBuffer)) {
            proxyOriginalNamesToReadableNames.put(
                    className, ClassFileUtilities.getReadableNameOfProxyClass(classfileBuffer));
            System.err.println("[VERIFIED]: " + className + " (regenerated from "
                    + String.join(", ", ClassFileUtilities.getInterfacesOfProxyClass(classfileBuffer)) + ")");
            return classfileBuffer;
        }
        // Only a fixed set of species shapes is baked into the JDK and recorded in the BOMI; the rest are spun at
        // runtime and are absent from it. A species is determined by its name, so it is checked by regenerating it.
        // A species that the specializer does not regenerate identically, like the hand-written root Species_L, falls
        // through to the lookup below, where the baked shapes are recorded.
        if (ClassFileUtilities.isSpeciesClass(classfileBuffer)
                && SpeciesRegenerator.isGeneratedByJdk(classfileBuffer)) {
            // Build the message without string concatenation: `+` compiles to an invokedynamic that spins a species
            // of its own, which would re-enter this transformer while a species is being defined.
            System.err.println(
                    new StringBuilder("[VERIFIED]: ").append(className).append(" (regenerated species)"));
            return classfileBuffer;
        }
        if (ClassFileUtilities.getSimpleNameFromQualifiedName(className).startsWith("GeneratedConstructorAccessor")) {
            String classForWhichTheConstructorIs =
                    ClassFileUtilities.getClassForWhichGeneratedAccessorIsFor(classfileBuffer);
            String isProxy = proxyOriginalNamesToReadableNames.get(classForWhichTheConstructorIs);
            String nameThatIsDisplayedInBomi;
            if (isProxy != null) {
                nameThatIsDisplayedInBomi = "GCA_" + isProxy;

            } else {
                nameThatIsDisplayedInBomi = "GCA_" + classForWhichTheConstructorIs;
            }
            return lookupReadableName(className, classfileBuffer, nameThatIsDisplayedInBomi);
        }
        // A class of a JDK module must be the one in the module image, which verifyJdk checked against the BOMI. This
        // catches classes patched into a JDK module. Classes absent from the image fall through to the lookup below.
        if (jdkAllowlisted) {
            byte[] inImage = JdkImage.read(module, className);
            if (inImage != null) {
                return Arrays.equals(inImage, classfileBuffer) ? classfileBuffer : modified(className, classfileBuffer);
            }
        }
        Set<ClassFileAttributes> candidates = fingerprints.get(className);
        if (candidates != null) {
            for (ClassFileAttributes candidate : candidates) {
                String hash = HashComputer.computeHash(classfileBuffer);
                if (hash.equals(candidate.hash())) {
                    return classfileBuffer;
                }
            }
            return modified(className, classfileBuffer);
        }
        return notAllowlisted(className, classfileBuffer);
    }

    private static byte[] lookupReadableName(String className, byte[] classfileBuffer, String correspondingClassName) {
        Set<ClassFileAttributes> attributes = fingerprints.get(correspondingClassName);
        if (attributes != null) {
            for (ClassFileAttributes attribute : attributes) {
                String hash = HashComputer.computeHash(classfileBuffer, proxyOriginalNamesToReadableNames);
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
