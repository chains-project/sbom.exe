package io.github.algomaster99.terminator.commons.fingerprint.constant_pool;

import static io.github.algomaster99.terminator.commons.fingerprint.constant_pool.ConstantPoolParser.CONSTANT_UTF8;

import java.util.Objects;

class Constant_Utf8 {
    final byte tag = CONSTANT_UTF8;

    short length;

    String bytes;

    int startPosition;

    public Constant_Utf8(short length, String bytes, int startPosition) {
        this.length = length;
        this.bytes = bytes;
        this.startPosition = startPosition;
    }

    public int getEndPosition() {
        return startPosition + 2 + length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Constant_Utf8 that = (Constant_Utf8) o;
        return tag == that.tag
                && length == that.length
                && startPosition == that.startPosition
                && Objects.equals(bytes, that.bytes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag, length, bytes, startPosition);
    }
}
