package io.github.algomaster99.terminator.commons.fingerprint.classfile;

import io.github.algomaster99.terminator.commons.fingerprint.constant_pool.ConstantPoolParser;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Formatter;

public class HashComputer {
    private HashComputer() {}

    public static String computeHash(byte[] bytes) {
        ConstantPoolParser parser = new ConstantPoolParser(bytes);
        parser.rewriteAllClassInfo()
                .rewriteSourceFileAttribute()
                .setNewName("Bar")
                .modify();
        byte[] rewrittenBytes = parser.getConstantPoolBytesOnly();

        Arrays.sort(rewrittenBytes);

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
