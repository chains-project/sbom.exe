package org.example;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

public class Terminator {
    static final File LOG_OF_CLASSES = new File("classes_javaagent.txt");
    static final List<String> CLASSES = new ArrayList<>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Files.write(LOG_OF_CLASSES.toPath(), CLASSES, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(
                new ClassFileTransformer() {
                    @Override
                    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
                        try {
                            return terminationCode(className, classfileBuffer);
                        } catch (NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        );
    }

    private static byte[] terminationCode(String className, byte[] classfileBuffer) throws NoSuchMethodException {
        CLASSES.add(className);
        return classfileBuffer;
    }
}
