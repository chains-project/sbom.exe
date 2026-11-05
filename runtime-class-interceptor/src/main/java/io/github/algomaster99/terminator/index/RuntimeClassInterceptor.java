package io.github.algomaster99.terminator.index;

import io.github.algomaster99.terminator.commons.fingerprint.JdkImage;
import io.github.algomaster99.terminator.commons.fingerprint.ParsingHelper;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileAttributes;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileUtilities;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.HashComputer;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ProxyRegenerator;
import io.github.algomaster99.terminator.commons.options.RuntimeClassInterceptorOptions;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RuntimeClassInterceptor {
    private static final Map<String, Set<ClassFileAttributes>> exhaustiveListOfClasses = new ConcurrentHashMap<>();

    // This is a map of proxy class names to their names with interfaces
    private static final Map<String, String> proxies = new ConcurrentHashMap<>();

    // Root of the project under test, or null to record classes from everywhere.
    private static Path project;

    // Whether a code source location holds production classes of the project, cached per location.
    private static final Map<URL, Boolean> projectLocations = new ConcurrentHashMap<>();

    public static void premain(String agentArgs, Instrumentation inst) {
        RuntimeClassInterceptorOptions options = new RuntimeClassInterceptorOptions(agentArgs);
        project = toRealPath(options.getProject());
        ProxyRegenerator.openGenerator(inst);
        JdkImage.preload();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ParsingHelper.serialiseFingerprints(exhaustiveListOfClasses, options.getOutput());
        }));
        inst.addTransformer(
                new ClassFileTransformer() {
                    @Override
                    public byte[] transform(
                            Module module,
                            ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer)
                            throws IllegalClassFormatException {
                        return recordClass(module, loader, className, protectionDomain, classfileBuffer);
                    }
                },
                false);
    }

    private static byte[] recordClass(
            Module module,
            ClassLoader loader,
            String className,
            ProtectionDomain protectionDomain,
            byte[] classfileBuffer) {
        // The watchdog checks the classes in the JDK's module image against the image, so they are not recorded.
        if (JdkImage.read(module, className) != null) {
            return classfileBuffer;
        }
        // The watchdog checks proxy classes by regenerating them, so they are not recorded.
        if (ClassFileUtilities.isProxyClass(classfileBuffer)
                && ProxyRegenerator.isGeneratedByJdk(loader, classfileBuffer)) {
            proxies.put(className, ClassFileUtilities.getReadableNameOfProxyClass(classfileBuffer));
            return classfileBuffer;
        }
        // Test classes and classes from jars outside the project, like test frameworks, Surefire and this agent, are
        // not recorded. Dependencies are indexed from the SBOM instead.
        if (!isLoadedFromProject(protectionDomain)) {
            return classfileBuffer;
        }
        // Test doubles share the protection domain of the mocked class but exist only in tests, under random names.
        if (isTestDouble(className)) {
            return classfileBuffer;
        }
        Set<ClassFileAttributes> candidates = exhaustiveListOfClasses.get(className);
        String classFileVersion = ClassFileUtilities.getVersion(classfileBuffer);
        String hash = HashComputer.computeHash(classfileBuffer);
        String newClassName = className;
        if (ClassFileUtilities.getSimpleNameFromQualifiedName(className).startsWith("GeneratedConstructorAccessor")) {
            String classForWhichTheConstructorIs =
                    ClassFileUtilities.getClassForWhichGeneratedAccessorIsFor(classfileBuffer);
            String isProxy = proxies.get(classForWhichTheConstructorIs);
            if (isProxy != null) {
                newClassName = "GCA_" + isProxy;
            } else {
                newClassName = "GCA_" + classForWhichTheConstructorIs;
            }
            // Like the watchdog, refer to proxies by their name in the BOMI because their numbered names vary.
            hash = HashComputer.computeHash(classfileBuffer, proxies);
        }
        if (candidates == null) {
            exhaustiveListOfClasses.put(
                    newClassName, new HashSet<>(Set.of(new ClassFileAttributes(classFileVersion, hash, "SHA-256"))));
        } else {
            candidates.add(new ClassFileAttributes(classFileVersion, hash, "SHA-256"));
            exhaustiveListOfClasses.put(newClassName, candidates);
        }
        return classfileBuffer;
    }

    // Names that Mockito and Byte Buddy give to the classes they generate, e.g. `TrueTypeFont$MockitoMock$4p5VBsG8`.
    static boolean isTestDouble(String className) {
        return className != null && (className.contains("$MockitoMock$") || className.contains("$ByteBuddy$"));
    }

    private static boolean isLoadedFromProject(ProtectionDomain protectionDomain) {
        if (project == null) {
            return true;
        }
        CodeSource codeSource = protectionDomain == null ? null : protectionDomain.getCodeSource();
        URL location = codeSource == null ? null : codeSource.getLocation();
        // Classes without a location are generated at runtime, so where they come from is unknown.
        if (location == null) {
            return true;
        }
        // No lambdas or computeIfAbsent: spinning their classes would re-enter the transformer.
        Boolean isFromProject = projectLocations.get(location);
        if (isFromProject == null) {
            isFromProject = isProductionCodeOfProject(location);
            projectLocations.put(location, isFromProject);
        }
        return isFromProject;
    }

    private static boolean isProductionCodeOfProject(URL location) {
        Path path;
        try {
            path = Path.of(location.toURI()).toRealPath();
        } catch (URISyntaxException | IOException | RuntimeException e) {
            // Locations that are not files on disk are kept, as before.
            return true;
        }
        if (!path.startsWith(project)) {
            return false;
        }
        for (Path name : project.relativize(path)) {
            if ("test-classes".equals(name.toString())) {
                return false;
            }
        }
        return true;
    }

    private static Path toRealPath(Path path) {
        if (path == null) {
            return null;
        }
        try {
            return path.toRealPath();
        } catch (IOException e) {
            return path.toAbsolutePath().normalize();
        }
    }
}
