package io.github.algomaster99.terminator.commons;

import static io.github.algomaster99.terminator.commons.HashComputer.toHexString;

import java.util.Arrays;

public class ClassfileVersion {

    // cafebabe
    private static final byte[] CLASSFILE_HEADER = new byte[] {-54, -2, -70, -66};

    private ClassfileVersion() {}

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
}
