package io.github.algomaster99.terminator.commons.fingerprint.constant_pool;

import static io.github.algomaster99.terminator.commons.fingerprint.constant_pool.ConstantPoolParser.CONSTANT_CLASS;

import java.util.Objects;

public class Constant_Class {
    final byte tag = CONSTANT_CLASS;
    short classIndex; // 2 bytes

    // extended fields
    int startPosition;

    public Constant_Class(short classIndex, int startPosition) {
        this.classIndex = classIndex;
        this.startPosition = startPosition;
    }

    public int getEndPosition() {
        return startPosition + Short.BYTES;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Constant_Class that = (Constant_Class) o;
        return tag == that.tag && classIndex == that.classIndex && startPosition == that.startPosition;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag, classIndex, startPosition);
    }
}
