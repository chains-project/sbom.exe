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
}
