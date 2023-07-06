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
            // Just to verify that the number of classes loaded in LOG_OF_CLASSES is correct
            System.out.println("Classes loaded: " + List.copyOf(CLASSES).size());
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
        String s = className + "\n";
        try {
            Files.writeString(LOG_OF_CLASSES.toPath(), s, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            CLASSES.add(className);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return classfileBuffer;
    }
}
