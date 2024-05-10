package io.github.algomaster99.terminator.index;

import io.github.algomaster99.terminator.commons.fingerprint.ParsingHelper;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileAttributes;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileUtilities;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.HashComputer;
import io.github.algomaster99.terminator.commons.options.RuntimeClassInterceptorOptions;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuntimeClassInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeClassInterceptor.class);
    private static final Map<String, Set<ClassFileAttributes>> exhaustiveListOfClasses = new ConcurrentHashMap<>();

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
        if (className.contains("com/sun/proxy/$Proxy")) {
            try {
                String moreReadableName = ClassFileUtilities.getNameForProxyClass(classfileBuffer);
                int number = Integer.parseInt(className.split("/")[3].substring(6));
                Files.write(Path.of("/home/aman/Desktop/chains/sbom.exe/$ProxyIndex" + moreReadableName + "_" + number + ".class"), classfileBuffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (className.startsWith("com/sun/proxy/$Proxy")) {
            className = ClassFileUtilities.getNameForProxyClass(classfileBuffer);
        }
        if (className.startsWith("jdk/internal/reflect/GeneratedConstructorAccessor")) {
            className = ClassFileUtilities.getClassNameForGeneratedConstructorAccessor(classfileBuffer);
        }
        if (candidates == null) {
            exhaustiveListOfClasses.put(className, Set.of(new ClassFileAttributes(classFileVersion, hash, "SHA-256")));
        } else {
            candidates.add(new ClassFileAttributes(classFileVersion, hash, "SHA-256"));
        }
        return classfileBuffer;
    }
}
