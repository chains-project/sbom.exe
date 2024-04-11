package io.github.algomaster99.terminator.commons.fingerprint.classfile;

import io.github.algomaster99.terminator.commons.fingerprint.constant_pool.ConstantPoolParser;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public class HashComputer {
    private HashComputer() {}

    public static String computeHash(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassWriter writer = new ClassWriter(reader, 0);
        reader.accept(writer, 0);

        byte[] rewrittenBytes = ConstantPoolParser.rewriteThisClass(bytes, "foo");

        byte[] algorithmSum;
        try {
            algorithmSum = MessageDigest.getInstance("SHA-256").digest(rewrittenBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
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
