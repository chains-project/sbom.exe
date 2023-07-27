package io.github.algomaster99.terminator.commons;

import static io.github.algomaster99.terminator.commons.HashComputer.toHexString;

import java.util.Arrays;

public class ClassfileVersion {

    // cafebabe
    private static final byte[] CLASSFILE_HEADER = new byte[] {-54, -2, -70, -66};

    private ClassfileVersion() {}

    public static int getVersion(byte[] bytes) {
        byte[] header = new byte[4];
        // copy the first 4 bytes
        System.arraycopy(bytes, 0, header, 0, 4);
        if (!Arrays.equals(header, CLASSFILE_HEADER)) {
            throw new IllegalArgumentException("Not a classfile");
        }
        return Integer.parseInt(toHexString(bytes[7]), 16);
    }
}
