package io.github.algomaster99.terminator.commons.fingerprint.classfile;

import static io.github.algomaster99.terminator.commons.fingerprint.classfile.HashComputer.toHexString;

import java.util.Arrays;
import org.objectweb.asm.ClassReader;

public class ClassFileUtilities {

    // cafebabe
    private static final byte[] CLASSFILE_HEADER = new byte[] {(byte) 0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe};

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
     * Returns a name for a proxy class that also includes the names of the interfaces that the proxy class implements.
     */
    public static String getNameForProxyClass(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        String[] interfaces = reader.getInterfaces();

        StringBuilder sb = new StringBuilder();
        sb.append("Proxy_");

        for (String i : interfaces) {
            sb.append(getSimpleNameFromQualifiedName(i));
        }

        return sb.toString();
    }

    private static String getSimpleNameFromQualifiedName(String qualifiedName) {
        return qualifiedName.substring(qualifiedName.lastIndexOf("/") + 1);
    }
}
