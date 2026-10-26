package io.github.algomaster99.terminator.commons.fingerprint.classfile;

import static io.github.algomaster99.terminator.commons.fingerprint.classfile.HashComputer.toHexString;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class ClassFileUtilities {

    // cafebabe
    private static final byte[] CLASSFILE_HEADER = new byte[] {(byte) 0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe};

    private static final String PROXY_SUPERCLASS = "java/lang/reflect/Proxy";

    private ClassFileUtilities() {}

    public static String getVersion(byte[] bytes) {
        byte[] header = new byte[4];
        // copy the first 4 bytes
        System.arraycopy(bytes, 0, header, 0, 4);
        if (!Arrays.equals(header, CLASSFILE_HEADER)) {
            throw new IllegalArgumentException("Not a classfile");
        }
        // See: https://en.wikipedia.org/wiki/Java_class_file
        // 2 bytes of minor version
        int minorVersion = Integer.parseInt(toHexString(new byte[] {bytes[4], bytes[5]}), 16);
        // 2 bytes of major version
        int majorVersion = Integer.parseInt(toHexString(new byte[] {bytes[6], bytes[7]}), 16);
        return String.format("%s.%s", majorVersion, minorVersion);
    }

    /**
     * Returns whether the class extends {@link java.lang.reflect.Proxy}, like every class that the JDK generates for
     * {@link java.lang.reflect.Proxy#newProxyInstance}. Unlike the package name of a proxy class, which changed from
     * {@code com.sun.proxy} to {@code jdk.proxyN} in JDK 17, the superclass is part of the specification.
     */
    public static boolean isProxyClass(byte[] bytes) {
        return PROXY_SUPERCLASS.equals(new ClassReader(bytes).getSuperName());
    }

    /**
     * Returns the interfaces that the proxy class implements, in the order they are declared.
     */
    public static List<String> getInterfacesOfProxyClass(byte[] bytes) {
        return List.of(new ClassReader(bytes).getInterfaces());
    }

    /**
     * Returns the name that a proxy class has in the keys of the BOMI.
     */
    public static String getReadableNameOfProxyClass(byte[] bytes) {
        // sort the interfaces to make the name deterministic
        return "Proxy_"
                + getInterfacesOfProxyClass(bytes).stream()
                        .sorted()
                        .map(i -> i.replace("/", "_"))
                        .collect(Collectors.joining());
    }

    /**
     * Returns the fully qualified name of the class that the Generated Constructor Accessor is for.
     */
    public static String getClassForWhichGeneratedAccessorIsFor(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode rootNode = new ClassNode();
        reader.accept(rootNode, 0);

        for (MethodNode method : rootNode.methods) {
            if (method.name.equals("newInstance")) {
                for (AbstractInsnNode insnNode : method.instructions) {
                    // this should be a NEW instruction
                    // GCA creates a new instance of the `desc` class
                    if (insnNode.getOpcode() == 187) {
                        return ((org.objectweb.asm.tree.TypeInsnNode) insnNode).desc;
                    }
                }
                return rootNode.name;
            }
        }
        System.err.println("This is a weird Generated Constructor Accessor: " + rootNode.name);
        // we exit violently because agent consumes exception
        Runtime.getRuntime().halt(1);
        throw new RuntimeException("This is a weird Generated Constructor Accessor: " + rootNode.name);
    }

    public static String getSimpleNameFromQualifiedName(String qualifiedName) {
        return qualifiedName.substring(qualifiedName.lastIndexOf("/") + 1);
    }
}
