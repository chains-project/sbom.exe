package io.github.algomaster99.terminator.commons;

import static io.github.algomaster99.terminator.commons.HashComputer.toHexString;

public class ClassfileVersion {

    private static final String CLASSFILE_HEADER = "cafebabe";

    private ClassfileVersion() {}

    public static int getVersion(byte[] bytes) {
        String hexString = toHexString(bytes);
        if (!hexString.substring(0, 8).startsWith(CLASSFILE_HEADER)) {
            throw new IllegalArgumentException("Not a classfile");
        }
        String majorVersion = hexString.substring(12, 16);
        return Integer.parseInt(majorVersion, 16);
    }
}
