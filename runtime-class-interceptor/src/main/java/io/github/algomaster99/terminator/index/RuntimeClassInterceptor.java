package io.github.algomaster99.terminator.index;

import io.github.algomaster99.terminator.commons.fingerprint.ParsingHelper;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileAttributes;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileUtilities;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.HashComputer;
import io.github.algomaster99.terminator.commons.options.RuntimeClassInterceptorOptions;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuntimeClassInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeClassInterceptor.class);
    private static final Map<String, Set<ClassFileAttributes>> exhaustiveListOfClasses = new ConcurrentHashMap<>();

    // This is a map of proxy class names to their names with interfaces
    private static final Map<String, String> proxies = new ConcurrentHashMap<>();

    public static void premain(String agentArgs, Instrumentation inst) {
        RuntimeClassInterceptorOptions options = new RuntimeClassInterceptorOptions(agentArgs);
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
                        return recordClass(className, classfileBuffer);
                    }
                },
                false);
    }

    private static byte[] recordClass(String className, byte[] classfileBuffer) {
        Set<ClassFileAttributes> candidates = exhaustiveListOfClasses.get(className);
        String classFileVersion = ClassFileUtilities.getVersion(classfileBuffer);
        String hash = HashComputer.computeHash(classfileBuffer);
        if (className.startsWith("com/sun/proxy/$Proxy") || className.startsWith("com/sun/proxy/jdk/")) {
            String nameThatNeedsToBeDisplayedInBomi =
                    "Proxy_" + ClassFileUtilities.getInterfacesOfProxyClass(classfileBuffer);
            proxies.put(className, nameThatNeedsToBeDisplayedInBomi);
            className = nameThatNeedsToBeDisplayedInBomi;
        } else if (className.startsWith("jdk/internal/reflect/GeneratedConstructorAccessor")) {
            String classForWhichTheConstructorIs =
                    ClassFileUtilities.getClassForWhichGeneratedAccessorIsFor(classfileBuffer);
            String isProxy = proxies.get(classForWhichTheConstructorIs);
            if (isProxy != null) {
                className = "GCA_" + isProxy;
            } else {
                className = "GCA_" + classForWhichTheConstructorIs;
            }
        }
        if (candidates == null) {
            exhaustiveListOfClasses.put(className, Set.of(new ClassFileAttributes(classFileVersion, hash, "SHA-256")));
        } else {
            candidates.add(new ClassFileAttributes(classFileVersion, hash, "SHA-256"));
        }
        return classfileBuffer;
    }
}
