package io.github.algomaster99.terminator.index;

import io.github.algomaster99.terminator.commons.fingerprint.ParsingHelper;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileAttributes;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassfileVersion;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.HashComputer;
import io.github.algomaster99.terminator.commons.fingerprint.provenance.Provenance;
import io.github.algomaster99.terminator.commons.fingerprint.provenance.RuntimeClass;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuntimeClassInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeClassInterceptor.class);
    private static final Map<String, Set<Provenance>> exhaustiveListOfClasses = new ConcurrentHashMap<>();

    public static void premain(String agentArgs, Instrumentation inst) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ParsingHelper.serialiseFingerprints(exhaustiveListOfClasses, Path.of("output.jsonl"));
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
        try {
            Set<Provenance> candidates = exhaustiveListOfClasses.get(className);
            String classFileVersion = ClassfileVersion.getVersion(classfileBuffer);
            String hash = HashComputer.computeHash(classfileBuffer, "SHA-256");
            if (candidates == null) {
                exhaustiveListOfClasses.put(
                        className,
                        Set.of(new RuntimeClass(new ClassFileAttributes(classFileVersion, hash, "SHA-256"), true)));
            } else {
                candidates.add(new RuntimeClass(new ClassFileAttributes(classFileVersion, hash, "SHA-256"), true));
            }
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("No such algorithm: " + e.getMessage());
            System.exit(1);
        }
        return classfileBuffer;
    }
}
