package io.github.algomaster99.terminator.index;

import io.github.algomaster99.terminator.commons.fingerprint.ParsingHelper;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassfileVersion;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.HashComputer;
import io.github.algomaster99.terminator.commons.fingerprint.protobuf.Bomi;
import io.github.algomaster99.terminator.commons.fingerprint.protobuf.ClassFile;
import io.github.algomaster99.terminator.commons.options.RuntimeClassInterceptorOptions;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuntimeClassInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeClassInterceptor.class);

    // we do not directly use the protobuf builder here because it is not thread-safe
    private static final Map<String, Set<ClassFileAttribute>> exhaustiveListOfClasses = new ConcurrentHashMap<>();

    public static void premain(String agentArgs, Instrumentation inst) {
        RuntimeClassInterceptorOptions options = new RuntimeClassInterceptorOptions(agentArgs);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Bomi.Builder bomiBuilder = Bomi.newBuilder();

            for (var entry : exhaustiveListOfClasses.entrySet()) {
                ClassFile.Builder classFileBuilder = ClassFile.newBuilder();
                classFileBuilder.setClassName(entry.getKey());

                for (var classFileAttribute : entry.getValue()) {
                    ClassFile.Attribute.Builder attributeBuilder = ClassFile.Attribute.newBuilder();

                    classFileBuilder.addAttribute(attributeBuilder
                            .setVersion(classFileAttribute.version)
                            .setHash(classFileAttribute.hash)
                            .build());
                }
                bomiBuilder.addClassFile(classFileBuilder.build());
            }
            ParsingHelper.serialiseFingerprints(bomiBuilder.build(), options.getOutput());
        }));
        inst.addTransformer(
                new ClassFileTransformer() {
                    @Override
                    public byte[] transform(
                            ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {
                        synchronized (exhaustiveListOfClasses) {
                            return recordClass(className, classfileBuffer);
                        }
                    }
                },
                false);
    }

    private static byte[] recordClass(String className, byte[] classfileBuffer) {
        Set<ClassFileAttribute> candidates = exhaustiveListOfClasses.get(className);
        String classFileVersion = ClassfileVersion.getVersion(classfileBuffer);
        String hash = HashComputer.computeHash(classfileBuffer);

        if (candidates == null) {
            exhaustiveListOfClasses.put(className, Set.of(new ClassFileAttribute(classFileVersion, hash)));

        } else {
            candidates.add(new ClassFileAttribute(classFileVersion, hash));
        }

        return classfileBuffer;
    }

    private static class ClassFileAttribute {
        private final String version;
        private final String hash;

        public ClassFileAttribute(String version, String hash) {
            this.version = version;
            this.hash = hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            ClassFileAttribute that = (ClassFileAttribute) obj;
            return Objects.equals(version, that.version) && Objects.equals(hash, that.hash);
        }

        @Override
        public int hashCode() {
            return Objects.hash(version, hash);
        }
    }
}
