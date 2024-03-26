package io.github.algomaster99;

import static io.github.algomaster99.terminator.commons.fingerprint.classfile.HashComputer.computeHash;

import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileAttributes;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.HashComputer;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.RuntimeClass;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.NoSuchAlgorithmException;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

public class Terminator {
    private static Options options;

    public static void premain(String agentArgs, Instrumentation inst) {
        options = new Options(agentArgs);
        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(
                    ClassLoader loader,
                    String className,
                    Class<?> classBeingRedefined,
                    ProtectionDomain protectionDomain,
                    byte[] classfileBuffer) {

                return isLoadedClassAllowlisted(className, classfileBuffer);
            }
        });
    }

    public static boolean isProxyClassRenamed(byte[] clasfileBytes, Map<String, String> proxies) {
        ClassReader reader = new ClassReader(clasfileBytes);
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, 0);

        boolean isInheritedFromProxy = classNode.superName.equals("java/lang/reflect/Proxy");

        if (!isInheritedFromProxy) {
            return false;
        }

        for (var proxy : proxies.entrySet()) {

            ClassNode nodeToBeRewrittenEvertIteration = new ClassNode();
            reader.accept(nodeToBeRewrittenEvertIteration, 0);

            String oldName = nodeToBeRewrittenEvertIteration.name;
            String fullyQualifiedClassName = proxy.getKey();

            renameClassNode(nodeToBeRewrittenEvertIteration, fullyQualifiedClassName);
            for (MethodNode methodNode : nodeToBeRewrittenEvertIteration.methods) {
                for (AbstractInsnNode instruction : methodNode.instructions) {
                    if (instruction instanceof FieldInsnNode) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) instruction;
                        if (fieldInsnNode.owner.equals(oldName)) {
                            fieldInsnNode.owner = fullyQualifiedClassName;
                        }
                    }
                }
            }
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            nodeToBeRewrittenEvertIteration.accept(writer);
            byte[] modifiedBytes = writer.toByteArray();
            try {

                if (Objects.equals(HashComputer.computeHash(modifiedBytes, "SHA-256"), proxy.getValue())) {
                    return true;
                }
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("No such algorithm: " + e.getMessage());
            }
        }
        return false;
    }

    public static boolean doesGeneratedAccessorUseADifferentProxyClass(
            byte[] classfileBytes, Map<String, String> accessors, Set<String> proxies) {
        ClassReader reader = new ClassReader(classfileBytes);
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, 0);

        boolean isInheritedFromMagicAccessor =
                classNode.superName.equals("jdk/internal/reflect/ConstructorAccessorImpl");

        if (!isInheritedFromMagicAccessor) {
            return false;
        }

        for (var accessor : accessors.entrySet()) {
            String fullyQualifiedClassName = accessor.getKey();

            renameClassNode(classNode, fullyQualifiedClassName);

            for (String proxy : proxies) {
                for (MethodNode methodNode : classNode.methods) {
                    for (AbstractInsnNode instruction : methodNode.instructions) {
                        if (instruction instanceof MethodInsnNode) {
                            MethodInsnNode methodInsnNode = (MethodInsnNode) instruction;
                            if (methodInsnNode.owner.startsWith("com/sun/proxy/$Proxy")) {
                                methodInsnNode.owner = proxy;
                            }
                        }
                        if (instruction instanceof TypeInsnNode) {
                            TypeInsnNode typeInsnNode = (TypeInsnNode) instruction;
                            if (typeInsnNode.desc.startsWith("com/sun/proxy/$Proxy")) {
                                typeInsnNode.desc = proxy;
                            }
                        }
                    }
                }
                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                classNode.accept(writer);
                byte[] modifiedBytes = writer.toByteArray();
                try {
                    if (Objects.equals(HashComputer.computeHash(modifiedBytes, "SHA-256"), accessor.getValue())) {
                        return true;
                    }
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException("No such algorithm: " + e.getMessage());
                }
            }
        }

        return false;
    }

    public static void renameClassNode(ClassNode classNode, String newName) {
        classNode.name = newName;
    }

    private static byte[] isLoadedClassAllowlisted(String className, byte[] classfileBuffer) {
        Map<String, Set<ClassFileAttributes>> fingerprints = options.getSbom();
        if (isProxyClassRenamed(classfileBuffer, getAllClassesStartingWith("com/sun/proxy/$Proxy", fingerprints))
                || doesGeneratedAccessorUseADifferentProxyClass(
                        classfileBuffer,
                        getAllClassesStartingWith("jdk/internal/reflect/GeneratedConstructorAccessor", fingerprints),
                        getAllProxies(fingerprints))
                || RuntimeClass.isBoundMethodHandle(classfileBuffer)) {
            return classfileBuffer;
        }
        for (String expectedClassName : fingerprints.keySet()) {
            if (expectedClassName.equals(className)) {
                Set<ClassFileAttributes> candidates = fingerprints.get(expectedClassName);
                for (ClassFileAttributes candidate : candidates) {
                    String hash;
                    try {
                        hash = computeHash(classfileBuffer, candidate.algorithm());
                    } catch (NoSuchAlgorithmException e) {
                        System.err.println("No such algorithm: " + e.getMessage());
                        System.exit(1);
                        return null;
                    }
                    if (hash.equals(candidate.hash())) {
                        return classfileBuffer;
                    }
                }
                if (options.shouldSkipShutdown()) {
                    System.err.println("[MODIFIED]: " + className);
                    return classfileBuffer;
                } else {
                    blueScreenOfDeath("[MODIFIED]: " + className);
                    System.exit(1);
                    return null;
                }
            }
        }
        if (options.shouldSkipShutdown()) {
            System.err.println("[NOT ALLOWLISTED]: " + className);
            return classfileBuffer;
        } else {
            blueScreenOfDeath("[NOT ALLOWLISTED]: " + className);
            System.exit(1);
            return null;
        }
    }

    private static Map<String, String> getAllClassesStartingWith(
            String prefix, Map<String, Set<ClassFileAttributes>> fingerprints) {
        Map<String, String> accessors = new ConcurrentHashMap<>();
        for (String className : fingerprints.keySet()) {
            if (className.contains(prefix)) {
                Set<ClassFileAttributes> candidates = fingerprints.get(className);
                accessors.put(className, candidates.stream().findFirst().get().hash());
            }
        }
        return accessors;
    }

    private static Set<String> getAllProxies(Map<String, Set<ClassFileAttributes>> fingerprints) {
        Set<String> proxies = ConcurrentHashMap.newKeySet();
        for (String className : fingerprints.keySet()) {
            if (className.startsWith("com/sun/proxy/$Proxy")) {
                proxies.add(className);
            }
        }
        return proxies;
    }

    //    com/sun/proxy/$Proxy0

    private static void blueScreenOfDeath(String classViolation) {
        final String WHITE = "\u001B[97m";
        final String BOLD = "\u001B[1m";
        final String BACKGROUND_LIGHT_BLUE = "\u001B[104m";
        final String RESET = "\u001B[0m";

        String message = "                \n" + "             _  \n"
                + "           .' ) \n"
                + " ,.--.    / .'  \n"
                + "//    \\  / /    \n"
                + "\\\\    / / /     \n"
                + " `'--' . '      \n"
                + " ,.--. | |      \n"
                + "//   \\' '       \n"
                + "\\\\    / \\ \\     \n"
                + " `'--'   \\ \\    \n"
                + "          \\ '.  \n"
                + "           '._) \n"
                + "\n"
                + "\n"
                + "A fatal error has been detected by the Java Runtime Environment:\n"
                + "\n"
                + classViolation
                + "\n";

        System.out.println(BACKGROUND_LIGHT_BLUE + BOLD + WHITE);
        System.out.println(message);
        System.out.println(RESET);
    }
}
