package io.github.algomaster99.terminator.commons.fingerprint.classfile;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public class HashComputer {
    private HashComputer() {}

    public static String computeHash(byte[] bytes, String algorithm) throws NoSuchAlgorithmException {
        ClassReader reader = new ClassReader(bytes);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        reader.accept(writer, 0);

        // apparently, ASM is not preserving the order of the constant pool table
        // see: https://stackoverflow.com/questions/78221149/asm-does-not-preserve-order-in-the-constant-pool-table
        byte[] modifiedBytesButShouldNotHaveBeen = writer.toByteArray();

        byte[] algorithmSum = MessageDigest.getInstance(algorithm).digest(modifiedBytesButShouldNotHaveBeen);
        return toHexString(algorithmSum);
    }

    public static String toHexString(byte[] bytes) {
        Formatter result = new Formatter();
        try (result) {
            for (byte b : bytes) {
                result.format(toHexString(b));
            }
            return result.toString();
        }
    }

    public static String toHexString(byte b) {
        return String.format("%02x", b & 0xff);
    }
}
