package io.github.algomaster99.terminator.commons.fingerprint.classfile;

import java.util.Objects;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

public class RuntimeClass {

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
     * Skip classes inherited from `jdk.internal.reflect.MagicAccessorImpl` because they are generated at runtime using ASM.
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

    /**
     * During the runtime the jvm binds methodhandles to the class `java.lang.invoke.BoundMethodHandle`.
     * These classes are generated at runtime and should be ignored. We can identify them by checking if the super class is `java.lang.invoke.BoundMethodHandle`.
     * @param classfileBytes  the class file bytes
     * @return  true if the class is a bound method handle, false otherwise.
     */
    public static boolean isBoundMethodHandle(byte[] classfileBytes) {
        ClassReader reader = new ClassReader(classfileBytes);
        return reader.getSuperName().equals("java/lang/invoke/BoundMethodHandle");
    }
}
