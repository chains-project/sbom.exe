package io.github.algomaster99.terminator.commons.fingerprint;

import io.github.algomaster99.terminator.commons.fingerprint.protobuf.Bomi;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ParsingHelper {
    private ParsingHelper() {}

    public static void serialiseFingerprints(Bomi bomi, Path fingerprintFile) {
        byte[] byteBuffer = bomi.toByteArray();
        try {
            Files.write(fingerprintFile, byteBuffer);
        } catch (IOException e) {
            throw new RuntimeException("Could not serialise protobuf: " + e);
        }
    }

    public static Bomi deserializeFingerprints(Path fingerprintFile) {
        try {
            byte[] byteBuffer = Files.readAllBytes(fingerprintFile);
            return Bomi.parseFrom(byteBuffer);
        } catch (IOException e) {
            throw new RuntimeException("Could not deserialize protobuf: " + e);
        }
    }
}
