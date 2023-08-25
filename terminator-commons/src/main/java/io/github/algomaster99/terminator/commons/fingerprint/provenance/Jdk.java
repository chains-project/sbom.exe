package io.github.algomaster99.terminator.commons.fingerprint.provenance;

import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileAttributes;

public class Jdk implements Provenance {

    private final ClassFileAttributes classFileAttributes;

    public Jdk(ClassFileAttributes classFileAttributes) {
        this.classFileAttributes = classFileAttributes;
    }

    @Override
    public ClassFileAttributes classFileAttributes() {
        return classFileAttributes;
    }
}
