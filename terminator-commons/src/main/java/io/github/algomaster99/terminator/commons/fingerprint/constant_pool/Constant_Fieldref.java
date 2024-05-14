package io.github.algomaster99.terminator.commons.fingerprint.constant_pool;

import static io.github.algomaster99.terminator.commons.fingerprint.constant_pool.ConstantPoolParser.CONSTANT_FIELDREF;

import java.util.Objects;

public class Constant_Fieldref implements ConstantPoolInfo {
    final byte tag = CONSTANT_FIELDREF;

    short constantPoolIndex; // constant pool index of the Fieldref_info structure
    short classIndex; // this points to a Class entry in the constant pool

    short nameAndTypeIndex; // this points to a NameAndType entry in the constant pool

    // extended fields
    int startPosition;

    public Constant_Fieldref(short classIndex, short nameAndTypeIndex, int startPosition, short constantPoolIndex) {
        this.classIndex = classIndex;
        this.nameAndTypeIndex = nameAndTypeIndex;
        this.startPosition = startPosition;
        this.constantPoolIndex = constantPoolIndex;
    }

    public int getEndPosition() {
        return startPosition + Short.BYTES + Short.BYTES;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Constant_Fieldref that = (Constant_Fieldref) o;
        return tag == that.tag
                && classIndex == that.classIndex
                && nameAndTypeIndex == that.nameAndTypeIndex
                && startPosition == that.startPosition
                && constantPoolIndex == that.constantPoolIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag, classIndex, nameAndTypeIndex, startPosition, constantPoolIndex);
    }

    @Override
    public String toString() {
        return "# " + constantPoolIndex + "Fieldref " + classIndex + ":" + nameAndTypeIndex;
    }

    @Override
    public short getConstantPoolIndex() {
        return classIndex;
    }
}
