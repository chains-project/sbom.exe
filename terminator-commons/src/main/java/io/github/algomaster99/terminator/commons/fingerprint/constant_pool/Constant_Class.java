package io.github.algomaster99.terminator.commons.fingerprint.constant_pool;

import static io.github.algomaster99.terminator.commons.fingerprint.constant_pool.ConstantPoolParser.CONSTANT_CLASS;

import java.util.Objects;

public class Constant_Class implements ConstantPoolInfo {
    final byte tag = CONSTANT_CLASS;

    short constantPoolIndex; // constant pool index of the Class_info structure
    short classIndex; // this points to a UTF8 entry in the constant pool

    // extended fields
    int startPosition;

    public Constant_Class(short classIndex, int startPosition, short constantPoolIndex) {
        this.classIndex = classIndex;
        this.startPosition = startPosition;
        this.constantPoolIndex = constantPoolIndex;
    }

    public int getEndPosition() {
        return startPosition + Short.BYTES;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Constant_Class that = (Constant_Class) o;
        return tag == that.tag
                && classIndex == that.classIndex
                && startPosition == that.startPosition
                && constantPoolIndex == that.constantPoolIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag, classIndex, startPosition, constantPoolIndex);
    }

    @Override
    public String toString() {
        return "# " + constantPoolIndex + "Class " + classIndex;
    }

    @Override
    public short getConstantPoolIndex() {
        return constantPoolIndex;
    }
}
