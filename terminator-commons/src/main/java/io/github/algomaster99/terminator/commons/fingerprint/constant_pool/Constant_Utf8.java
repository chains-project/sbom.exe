package io.github.algomaster99.terminator.commons.fingerprint.constant_pool;

import static io.github.algomaster99.terminator.commons.fingerprint.constant_pool.ConstantPoolParser.CONSTANT_UTF8;

import java.util.Objects;

class Constant_Utf8 implements Comparable<Constant_Utf8>, ConstantPoolInfo {
    final byte tag = CONSTANT_UTF8;

    short constantPoolIndex; // constant pool index of the UTF8_info structure

    short length;

    String bytes;

    int startPosition;

    public Constant_Utf8(short length, String bytes, int startPosition, short constantPoolIndex) {
        this.length = length;
        this.bytes = bytes;
        this.startPosition = startPosition;
        this.constantPoolIndex = constantPoolIndex;
    }

    /**
     * Used for rewriting the constant pool.
     */
    public void setBytes(String bytes) {
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes.getBytes();
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

    @Override
    public int compareTo(Constant_Utf8 o) {
        return startPosition - o.startPosition;
    }

    @Override
    public String toString() {
        return bytes;
    }

    @Override
    public short getConstantPoolIndex() {
        return constantPoolIndex;
    }
}
