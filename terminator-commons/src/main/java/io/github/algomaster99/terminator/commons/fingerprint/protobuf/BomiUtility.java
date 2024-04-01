package io.github.algomaster99.terminator.commons.fingerprint.protobuf;

import java.util.Optional;

public class BomiUtility {
    public static Optional<ClassFile> isClassFilePresent(Bomi.Builder bomiBuilder, String className) {
        for (ClassFile classFile : bomiBuilder.getClassFileList()) {
            if (classFile.getClassName().equals(className)) {
                return Optional.of(classFile);
            }
        }
        return Optional.empty();
    }

    public static boolean isHashSame(ClassFile classFile, String hash) {
        return classFile.getAttributeList().stream()
                .map(ClassFile.Attribute::getHash)
                .anyMatch(h -> h.equals(hash));
    }
}
