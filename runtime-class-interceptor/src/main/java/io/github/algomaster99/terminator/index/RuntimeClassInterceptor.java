package io.github.algomaster99.terminator.index;

import io.github.algomaster99.terminator.commons.fingerprint.ParsingHelper;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileAttributes;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileUtilities;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.HashComputer;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ProxyRegenerator;
import io.github.algomaster99.terminator.commons.options.RuntimeClassInterceptorOptions;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RuntimeClassInterceptor {
    private static final Map<String, Set<ClassFileAttributes>> exhaustiveListOfClasses = new ConcurrentHashMap<>();

    // This is a map of proxy class names to their names with interfaces
    private static final Map<String, String> proxies = new ConcurrentHashMap<>();

    public static void premain(String agentArgs, Instrumentation inst) {
        RuntimeClassInterceptorOptions options = new RuntimeClassInterceptorOptions(agentArgs);
        ProxyRegenerator.openGenerator(inst);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ParsingHelper.serialiseFingerprints(exhaustiveListOfClasses, options.getOutput());
        }));
        inst.addTransformer(
                new ClassFileTransformer() {
                    @Override
                    public byte[] transform(
                            ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer)
                            throws IllegalClassFormatException {
                        return recordClass(loader, className, classfileBuffer);
                    }
                },
                false);
    }

    private static byte[] recordClass(ClassLoader loader, String className, byte[] classfileBuffer) {
        // The watchdog checks proxy classes by regenerating them, so they are not recorded.
        if (ClassFileUtilities.isProxyClass(classfileBuffer)
                && ProxyRegenerator.isGeneratedByJdk(loader, classfileBuffer)) {
            proxies.put(className, "Proxy_" + ClassFileUtilities.getInterfacesOfProxyClass(classfileBuffer));
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
}
