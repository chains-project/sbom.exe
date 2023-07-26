package io.github.algomaster99.terminator.commons;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class HashComputer {
    private HashComputer() {}

    public static String computeHash(byte[] bytes, String algorithm) throws NoSuchAlgorithmException {
        byte[] algorithmSum = MessageDigest.getInstance(algorithm).digest(bytes);
        return toHexString(algorithmSum);
    }

    private static String toHexString(byte[] bytes) {
        Formatter result = new Formatter();
        try (result) {
            for (byte b : bytes) {
                result.format("%02x", b & 0xff);
            }
            return result.toString();
        }
    }
}
