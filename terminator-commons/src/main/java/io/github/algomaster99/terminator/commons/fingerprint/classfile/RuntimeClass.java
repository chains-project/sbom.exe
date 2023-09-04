package io.github.algomaster99.terminator.commons.fingerprint.classfile;

import java.util.List;
import java.util.Objects;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

public class RuntimeClass {

    private static List<String> whitelistedClasses = List.of(
            "jdk/internal/reflect/UnsafeQualifiedStaticFieldAccessorImpl",
            "jdk/internal/reflect/UnsafeStaticFieldAccessorImpl",
            "jdk/internal/reflect/UnsafeFieldAccessorImpl");

    private RuntimeClass() {}

    /**
     * Proxy classes are not synthetic classes, but they are runtime generated.
     */
    public static boolean isProxyClass(byte[] classfileBytes) {
        ClassReader reader = new ClassReader(classfileBytes);
        return Objects.equals(reader.getSuperName(), "java/lang/reflect/Proxy");
    }

    public static boolean isSynthetic(byte[] classfileBytes) {
        ClassReader reader = new ClassReader(classfileBytes);
        return (reader.getAccess() & Opcodes.ACC_SYNTHETIC) != 0;
    }

    /**
     * Skip classes inherited from {@link jdk.internal.reflect.MagicAccessorImpl} because they are generated at runtime using ASM.
     */
    public static boolean isGeneratedClassExtendingMagicAccessor(byte[] classfileBytes) {
        ClassReader reader = new ClassReader(classfileBytes);
        try {
            return RuntimeClass.class
                    .getClassLoader()
                    .loadClass(reader.getSuperName().replace("/", "."))
                    .getSuperclass()
                    .getName()
                    .equals("jdk.internal.reflect.MagicAccessorImpl");
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    // will be removed in the future https://github.com/openjdk/jdk/commit/9bfe415f66cc169249d83fc161c9c4496fe239f6
    public static boolean isUnsafeQualifiedStaticAccess(byte[] classfileBytes) {
        ClassReader reader = new ClassReader(classfileBytes);
        try {
            return whitelistedClasses.contains(RuntimeClass.class
                    .getClassLoader()
                    .loadClass(reader.getSuperName().replace("/", "."))
                    .getSuperclass()
                    .getName());
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found: " + e.getMessage());
            return false;
        }
    }

    public static boolean isWhitelistedClass(String className) {
        return whitelistedClasses.contains(className);
    }
}
