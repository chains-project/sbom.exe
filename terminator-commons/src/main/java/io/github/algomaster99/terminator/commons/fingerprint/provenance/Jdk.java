package io.github.algomaster99.terminator.commons.fingerprint.provenance;

import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileAttributes;

public record Jdk(ClassFileAttributes classFileAttributes) implements Provenance {
    @Override
    public ClassFileAttributes classFileAttributes() {
        return classFileAttributes;
    }
}
