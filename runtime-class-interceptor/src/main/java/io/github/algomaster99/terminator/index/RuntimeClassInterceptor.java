package io.github.algomaster99.terminator.index;

import io.github.algomaster99.terminator.commons.fingerprint.JdkImage;
import io.github.algomaster99.terminator.commons.fingerprint.ParsingHelper;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileAttributes;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileUtilities;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.HashComputer;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ProxyRegenerator;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.SpeciesRegenerator;
import io.github.algomaster99.terminator.commons.options.RuntimeClassInterceptorOptions;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class RuntimeClassInterceptor {
    private static final Map<String, Set<ClassFileAttributes>> exhaustiveListOfClasses = new ConcurrentHashMap<>();

    // This is a map of proxy class names to their names with interfaces
    private static final Map<String, String> proxies = new ConcurrentHashMap<>();

    // Root of the project under test, or null to record classes from everywhere.
    private static Path project;

    // What a code source location holds, cached per location.
    private static final Map<URL, Origin> origins = new ConcurrentHashMap<>();

    private enum Origin {
        PRODUCTION_CODE,
        TEST_CODE,
        OUTSIDE_PROJECT
    }

    // Jars of the project, kept open to look up class files.
    private static final Map<Path, JarFile> jars = new ConcurrentHashMap<>();

    // `RETAIN_CLASS_REFERENCE` is needed as otherwise, only strings are kept not the class object
    // Class object has ProtectionDomain which is needed
    private static final StackWalker WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    // Created up front: loading its class inside the transformer would re-enter it.
    private static final Function<Stream<StackWalker.StackFrame>, Boolean> NEAREST_PROJECT_FRAME_IS_TEST =
            new NearestProjectFrameIsTest();

    public static void premain(String agentArgs, Instrumentation inst) {
        RuntimeClassInterceptorOptions options = new RuntimeClassInterceptorOptions(agentArgs);
        project = toRealPath(options.getProject());
        ProxyRegenerator.openGenerator(inst);
        SpeciesRegenerator.openGenerator(inst);
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
        // The watchdog checks species classes that the JDK spins by regenerating them, so they are not recorded.
        if (ClassFileUtilities.isSpeciesClass(classfileBuffer)
                && SpeciesRegenerator.isGeneratedByJdk(classfileBuffer)) {
            return classfileBuffer;
        }
        // Test classes and classes from jars outside the project, like test frameworks, Surefire and this agent, are
        // not recorded. Dependencies are indexed from the SBOM instead.
        if (!isLoadedFromProject(protectionDomain)) {
            return classfileBuffer;
        }
        // Classes that tests generate, like mocks, share the protection domain of the class they extend.
        if (isGeneratedForTest(className, protectionDomain)) {
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

    private static boolean isGeneratedForTest(String className, ProtectionDomain protectionDomain) {
        // Classes the JDK spins without a protection domain, like `GeneratedConstructorAccessor`, are recorded.
        // Classes with a class file in the project, like `TrueTypeFont`, are recorded too.
        if (project == null || protectionDomain == null || hasClassFile(locationOf(protectionDomain), className)) {
            return false;
        }
        // The nearest frame from the project tells who asked for the class: a test or production code.
        return WALKER.walk(NEAREST_PROJECT_FRAME_IS_TEST);
    }

    static boolean hasClassFile(URL location, String className) {
        if (location == null) {
            return false;
        }
        String classFile = className + ".class";
        try {
            Path path = Path.of(location.toURI());
            // A classes directory, like `target/classes`. `TrueTypeFont` has its class file there, but a mock of it,
            // `TrueTypeFont$MockitoMock$4p5VBsG8`, shares the location without one.
            if (Files.isDirectory(path)) {
                return Files.isRegularFile(path.resolve(classFile));
            }
            // Modules of a multi-module project see each other as jars in their `target` directories.
            return openJar(path).getEntry(classFile) != null;
        } catch (URISyntaxException | IOException e) {
            throw new IllegalArgumentException(location.toString(), e);
        }
    }

    // No lambdas or computeIfAbsent: spinning their classes would re-enter the transformer.
    private static JarFile openJar(Path path) throws IOException {
        JarFile jar = jars.get(path);
        if (jar == null) {
            jar = new JarFile(path.toFile());
            JarFile opened = jars.putIfAbsent(path, jar);
            if (opened != null) {
                jar.close();
                jar = opened;
            }
        }
        return jar;
    }

    // A class, not a lambda: spinning a lambda's class would re-enter the transformer.
    private static final class NearestProjectFrameIsTest implements Function<Stream<StackWalker.StackFrame>, Boolean> {
        @Override
        public Boolean apply(Stream<StackWalker.StackFrame> frames) {
            Iterator<StackWalker.StackFrame> iterator = frames.iterator();
            while (iterator.hasNext()) {
                URL location = locationOf(iterator.next().getDeclaringClass().getProtectionDomain());
                // Frames without a location, like the JDK's, are not from the project.
                // so the stack walk is continued.
                Origin origin = location == null ? Origin.OUTSIDE_PROJECT : originOf(location);
                if (origin != Origin.OUTSIDE_PROJECT) {
                    return origin == Origin.TEST_CODE;
                }
            }
            return false;
        }
    }

    private static boolean isLoadedFromProject(ProtectionDomain protectionDomain) {
        if (project == null) {
            return true;
        }
        URL location = locationOf(protectionDomain);
        // Classes without a location are generated at runtime, so where they come from is unknown.
        if (location == null) {
            return true;
        }
        return originOf(location) == Origin.PRODUCTION_CODE;
    }

    private static URL locationOf(ProtectionDomain protectionDomain) {
        CodeSource codeSource = protectionDomain == null ? null : protectionDomain.getCodeSource();
        return codeSource == null ? null : codeSource.getLocation();
    }

    private static Origin originOf(URL location) {
        // No lambdas or computeIfAbsent: spinning their classes would re-enter the transformer.
        Origin origin = origins.get(location);
        if (origin == null) {
            origin = findOrigin(location);
            origins.put(location, origin);
        }
        return origin;
    }

    private static Origin findOrigin(URL location) {
        Path path;
        try {
            path = Path.of(location.toURI()).toRealPath();
        } catch (URISyntaxException | IOException | RuntimeException e) {
            // Locations that are not files on disk are kept, as before.
            return Origin.PRODUCTION_CODE;
        }
        if (!path.startsWith(project)) {
            return Origin.OUTSIDE_PROJECT;
        }
        for (Path name : project.relativize(path)) {
            if ("test-classes".equals(name.toString())) {
                return Origin.TEST_CODE;
            }
        }
        return Origin.PRODUCTION_CODE;
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
