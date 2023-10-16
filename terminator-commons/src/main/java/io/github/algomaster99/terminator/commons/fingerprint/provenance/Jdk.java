package io.github.algomaster99.terminator.commons.fingerprint.provenance;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileAttributes;

@JsonDeserialize(as = Jdk.class)
public record Jdk(ClassFileAttributes classFileAttributes) implements Provenance {
    @Override
    public ClassFileAttributes classFileAttributes() {
        return classFileAttributes;
    }
}
