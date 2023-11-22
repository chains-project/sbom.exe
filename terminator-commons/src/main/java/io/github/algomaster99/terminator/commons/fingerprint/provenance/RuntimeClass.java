package io.github.algomaster99.terminator.commons.fingerprint.provenance;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileAttributes;

@JsonDeserialize(as = RuntimeClass.class)
public record RuntimeClass(ClassFileAttributes classFileAttributes, boolean runtime) implements Provenance {
    @Override
    public boolean runtime() {
        return true;
    }
}
