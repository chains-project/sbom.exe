package io.github.algomaster99.terminator.index;

import io.github.algomaster99.terminator.commons.fingerprint.ParsingHelper;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassfileVersion;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.HashComputer;
import io.github.algomaster99.terminator.commons.fingerprint.protobuf.Bomi;
import io.github.algomaster99.terminator.commons.fingerprint.protobuf.BomiUtility;
import io.github.algomaster99.terminator.commons.fingerprint.protobuf.ClassFile;
import io.github.algomaster99.terminator.commons.options.RuntimeClassInterceptorOptions;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuntimeClassInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeClassInterceptor.class);
    private static final Bomi.Builder exhaustiveListOfClasses = Bomi.newBuilder();

    public static void premain(String agentArgs, Instrumentation inst) {
        RuntimeClassInterceptorOptions options = new RuntimeClassInterceptorOptions(agentArgs);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            synchronized (exhaustiveListOfClasses) {
                ParsingHelper.serialiseFingerprints(exhaustiveListOfClasses.build(), options.getOutput());
            }
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
        Optional<ClassFile> classFileCanidate = BomiUtility.isClassFilePresent(exhaustiveListOfClasses, className);
        String classFileVersion = ClassfileVersion.getVersion(classfileBuffer);
        String hash = HashComputer.computeHash(classfileBuffer);

        if (classFileCanidate.isPresent()) {
            ClassFile classFile = classFileCanidate.get();

            if (BomiUtility.isHashSame(classFile, hash)) {
                return classfileBuffer;
            }

            int indexOfClassFile = exhaustiveListOfClasses.getClassFileList().indexOf(classFile);

            ClassFile.Builder classFileBuilder = ClassFile.newBuilder();
            classFileBuilder.mergeFrom(classFile);

            classFileBuilder.addAttribute(ClassFile.Attribute.newBuilder()
                    .setVersion(classFileVersion)
                    .setHash(hash)
                    .build());

            exhaustiveListOfClasses.removeClassFile(indexOfClassFile);
            exhaustiveListOfClasses.addClassFile(indexOfClassFile, classFileBuilder.build());
        } else {
            ClassFile.Builder classFileBuilder = ClassFile.newBuilder();
            classFileBuilder
                    .setClassName(className)
                    .addAttribute(ClassFile.Attribute.newBuilder()
                            .setVersion(classFileVersion)
                            .setHash(hash)
                            .build());

            exhaustiveListOfClasses.addClassFile(classFileBuilder.build());
        }

        return classfileBuffer;
    }
}
