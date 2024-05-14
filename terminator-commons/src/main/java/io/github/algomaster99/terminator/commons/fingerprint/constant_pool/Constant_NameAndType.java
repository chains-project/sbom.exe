package io.github.algomaster99.terminator.commons.fingerprint.constant_pool;

import static io.github.algomaster99.terminator.commons.fingerprint.constant_pool.ConstantPoolParser.CONSTANT_NAME_AND_TYPE;

import java.util.Objects;

public class Constant_NameAndType implements ConstantPoolInfo {
    final byte tag = CONSTANT_NAME_AND_TYPE;

    short constantPoolIndex; // constant pool index of the NameAndType_info structure
    short nameIndex; // this points to a UTF8 entry in the constant pool

    short typeIndex; // this points to a UTF8 entry in the constant pool

    // extended fields
    int startPosition;

    public Constant_NameAndType(short nameIndex, short typeIndex, int startPosition, short constantPoolIndex) {
        this.nameIndex = nameIndex;
        this.typeIndex = typeIndex;
        this.startPosition = startPosition;
        this.constantPoolIndex = constantPoolIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Constant_NameAndType that = (Constant_NameAndType) o;
        return tag == that.tag
                && nameIndex == that.nameIndex
                && typeIndex == that.typeIndex
                && startPosition == that.startPosition
                && constantPoolIndex == that.constantPoolIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag, nameIndex, typeIndex, startPosition, constantPoolIndex);
    }

    @Override
    public String toString() {
        return "# " + constantPoolIndex + "NameAndType " + nameIndex + ":" + typeIndex;
    }

    @Override
    public short getConstantPoolIndex() {
        return constantPoolIndex;
    }
}
