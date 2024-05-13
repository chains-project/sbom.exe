package io.github.algomaster99.terminator.commons.fingerprint.constant_pool;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * A small parser to read the constant pool directly, in case it contains references ASM does not support.
 *
 * <p>Adapted from <a href="http://stackoverflow.com/a/32278587/23691">...</a>
 *
 * <p>Constant pool types:
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se11/html/jvms-4.html#jvms-4.4">JVM 11 Sepc</a>
 */
public class ConstantPoolParser {
    static final int HEAD = 0xcafebabe;

    // Constant pool types
    static final byte CONSTANT_UTF8 = 1;

    static final byte CONSTANT_INTEGER = 3;

    static final byte CONSTANT_FLOAT = 4;

    static final byte CONSTANT_LONG = 5;

    static final byte CONSTANT_DOUBLE = 6;

    static final byte CONSTANT_CLASS = 7;

    static final byte CONSTANT_STRING = 8;

    static final byte CONSTANT_FIELDREF = 9;

    static final byte CONSTANT_METHODREF = 10;

    static final byte CONSTANT_INTERFACEMETHODREF = 11;

    static final byte CONSTANT_NAME_AND_TYPE = 12;

    static final byte CONSTANT_METHODHANDLE = 15;

    static final byte CONSTANT_METHOD_TYPE = 16;

    static final byte CONSTANT_INVOKE_DYNAMIC = 18;

    static final byte CONSTANT_MODULE = 19;

    static final byte CONSTANT_PACKAGE = 20;

    // attributes
    private static final String ATTRIBUTE_SOURCE_FILE = "SourceFile";

    private static final int OXF0 = 0xf0;

    private static final int OXE0 = 0xe0;

    private static final int OX3F = 0x3F;

    private byte[] bytecode;

    final Map<Short, Constant_Utf8> cpIndexToConstantUtf8 = new HashMap<>();
    final Set<Constant_Class> classInfo = new HashSet<>();
    final Set<Constant_Fieldref> fieldRefInfo = new HashSet<>();
    final Set<Constant_NameAndType> nameAndTypeInfo = new HashSet<>();

    final Set<Constant_Utf8> toBeModifiedUtf8Entries = new TreeSet<>();

    Constant_Utf8 sourceFileValue;

    int constantPoolEndPosition;

    short thisClassIndex;

    String newName;

    public ConstantPoolParser(byte[] bytes) {
        this(ByteBuffer.wrap(bytes));
        this.bytecode = bytes;
    }

    private ConstantPoolParser(ByteBuffer buf) {
        if (buf.order(ByteOrder.BIG_ENDIAN).getInt() != HEAD) {
            throw new RuntimeException("Not a valid classfile");
        }
        buf.getChar();
        buf.getChar(); // minor + ver
        for (int ix = 1, num = buf.getShort(); ix < num; ix++) {
            byte tag = buf.get();
            int startPosition = buf.position();
            switch (tag) {
                case CONSTANT_UTF8:
                    cpIndexToConstantUtf8.put((short) ix, createUtf8Entry(buf, startPosition, (short) ix));
                    break;
                case CONSTANT_CLASS:
                    classInfo.add(new Constant_Class(buf.getShort(), startPosition, (short) ix));
                    break;
                case CONSTANT_STRING:
                case CONSTANT_METHOD_TYPE:
                    buf.getChar();
                    break;
                case CONSTANT_FIELDREF:
                    fieldRefInfo.add(new Constant_Fieldref(buf.getShort(), buf.getShort(), startPosition, (short) ix));
                    break;
                case CONSTANT_METHODREF:
                case CONSTANT_INTERFACEMETHODREF:
                    buf.getShort(); // gets the class index
                    buf.getChar();
                    break;
                case CONSTANT_NAME_AND_TYPE:
                    nameAndTypeInfo.add(
                            new Constant_NameAndType(buf.getShort(), buf.getShort(), startPosition, (short) ix));
                    break;
                case CONSTANT_INVOKE_DYNAMIC:
                    buf.getChar();
                    buf.getChar();
                    break;
                case CONSTANT_INTEGER:
                    buf.getInt();
                    break;
                case CONSTANT_FLOAT:
                    buf.getFloat();
                    break;
                case CONSTANT_DOUBLE:
                    buf.getDouble();
                    ix++;
                    break;
                case CONSTANT_LONG:
                    buf.getLong();
                    ix++;
                    break;
                case CONSTANT_METHODHANDLE:
                    buf.get();
                    buf.getChar();
                    break;
                case CONSTANT_MODULE:
                case CONSTANT_PACKAGE:
                    buf.getChar();
                    break;
                default:
                    throw new RuntimeException("Unknown constant pool type '" + tag + "'");
            }
        }
        constantPoolEndPosition = buf.position();
        buf.getShort(); // access flags
        Map<Short, Constant_Class> cpIndexToConstantClass = setToMap(classInfo);
        thisClassIndex = cpIndexToConstantClass.get(buf.getShort()).classIndex; // this class
        buf.getShort(); // super class
        int interfaceCount = buf.getShort(); // interface_count
        for (int ix = 0, num = interfaceCount; ix < num; ix++) {
            buf.getShort(); // interface index (into Constant_Class)
        }

        int fieldCount = buf.getShort(); // fields_count
        for (int ix = 0, num = fieldCount; ix < num; ix++) {
            buf.getShort(); // access flags
            buf.getShort(); // name index
            buf.getShort(); // descriptor index
            int attributeCount = buf.getShort(); // attributes count
            for (int jx = 0, attrCount = attributeCount; jx < attrCount; jx++) {
                buf.getShort(); // attribute name index
                int attrLength = buf.getInt(); // attribute length
                for (int kx = 0, len = attrLength; kx < len; kx++) {
                    buf.get(); // info
                }
            }
        }

        int methodCount = buf.getShort(); // methods_count
        for (int ix = 0; ix < methodCount; ix++) {
            buf.getShort(); // access flags
            buf.getShort(); // name index
            buf.getShort(); // descriptor index
            int attributeCount = buf.getShort(); // attributes count
            for (int jx = 0; jx < attributeCount; jx++) {
                buf.getShort(); // attribute name index
                int attrLength = buf.getInt(); // attribute length
                // TODO: consider bytecode opcodes for canonicalization
                for (int kx = 0; kx < attrLength; kx++) {
                    buf.get(); // info
                }
            }
        }

        int attributeCount = buf.getShort(); // attributes count
        for (int ix = 0, num = attributeCount; ix < num; ix++) {
            short indexOfAttributeName = buf.getShort(); // attribute name index
            switch (cpIndexToConstantUtf8.get(indexOfAttributeName).bytes) {
                case ATTRIBUTE_SOURCE_FILE:
                    buf.getInt(); // attribute length
                    short indexOfSourceFileValue = buf.getShort(); // source file index
                    sourceFileValue = cpIndexToConstantUtf8.get(indexOfSourceFileValue);
                    break;
                default:
                    int attrLength = buf.getInt(); // attribute length
                    for (int kx = 0, len = attrLength; kx < len; kx++) {
                        buf.get(); // info
                    }
                    break;
            }
        }
        buf.rewind();
    }

    private static Constant_Utf8 createUtf8Entry(ByteBuffer buf, int startPosition, short index) {
        int size = buf.getChar();
        int oldLimit = buf.limit();
        buf.limit(buf.position() + size);
        StringBuilder sb = new StringBuilder(size + (size >> 1) + 16);
        while (buf.hasRemaining()) {

            byte b = buf.get();
            if (b > 0) {
                sb.append((char) b);
            } else {
                int b2 = buf.get();
                if ((b & OXF0) != OXE0) {
                    sb.append((char) ((b & 0x1F) << 6 | b2 & OX3F));
                } else {
                    int b3 = buf.get();
                    sb.append((char) ((b & 0x0F) << 12 | (b2 & OX3F) << 6 | b3 & OX3F));
                }
            }
        }
        buf.limit(oldLimit);
        String utf8Value = sb.toString();

        return new Constant_Utf8((short) size, utf8Value, startPosition, index);
    }

    // TODO: not sure why ? extends T works
    public static <T extends ConstantPoolInfo> Map<Short, T> setToMap(Set<? extends T> entries) {
        Map<Short, T> map = new HashMap<>();
        for (T entry : entries) {
            map.put(entry.getConstantPoolIndex(), entry);
        }
        return map;
    }

    public byte[] getConstantPoolBytesOnly() {
        return getConstantPoolEntries();
    }

    private byte[] getConstantPoolEntries() {
        StringBuilder sb = new StringBuilder();
        cpIndexToConstantUtf8.forEach((k, v) -> {
            sb.append(v.bytes);
        });
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public ConstantPoolParser rewriteAllClassInfo() {
        for (Constant_Class constantClass : classInfo) {
            Constant_Utf8 utf8 = cpIndexToConstantUtf8.get(constantClass.classIndex);
            toBeModifiedUtf8Entries.add(utf8);
        }
        return this;
    }

    public ConstantPoolParser rewriteAllFieldRef() {
        Map<Short, Constant_NameAndType> cpIndexToConstantNameAndType = setToMap(nameAndTypeInfo);
        for (Constant_Fieldref fieldref : fieldRefInfo) {
            Constant_NameAndType nameAndType = cpIndexToConstantNameAndType.get(fieldref.nameAndTypeIndex);
            Constant_Utf8 utf8 = cpIndexToConstantUtf8.get(nameAndType.nameIndex);
            toBeModifiedUtf8Entries.add(utf8);
        }
        return this;
    }

    public String getThisClassName() {
        return this.cpIndexToConstantUtf8.get(this.thisClassIndex).bytes;
    }

    public ConstantPoolParser setNewName(String newName) {
        this.newName = newName;
        return this;
    }

    public ConstantPoolParser rewriteSourceFileAttribute() {
        // generated classes do not have SourceFile attribute
        if (sourceFileValue != null) {
            toBeModifiedUtf8Entries.add(sourceFileValue);
        }
        return this;
    }

    public void modify() {
        modifyUtfEntries(ByteBuffer.wrap(bytecode));
    }

    private void modifyUtfEntries(ByteBuffer buf) {
        int sizeOfAllUtf8Entries =
                toBeModifiedUtf8Entries.stream().mapToInt(u -> u.length).sum();

        int newNameByteSize = newName.getBytes(StandardCharsets.UTF_8).length;

        int oldBufferSize = buf.limit();

        int newBufferSize = oldBufferSize - sizeOfAllUtf8Entries + newNameByteSize * toBeModifiedUtf8Entries.size();

        ByteBuffer byteBuffer = ByteBuffer.allocate(newBufferSize);
        int oldBufferIndexToCopyFrom = 0;

        for (Constant_Utf8 utf8Entry : toBeModifiedUtf8Entries) {
            // copy the bytes from the old buffer to the new buffer until the start of the current utf8 entry
            byteBuffer.put(buf.array(), oldBufferIndexToCopyFrom, utf8Entry.startPosition - oldBufferIndexToCopyFrom);
            // write the size of the new name
            int newPosition = byteBuffer.position();
            byteBuffer.putShort(newPosition, (short) newNameByteSize);
            // update the position after writing the size in the new buffer
            byteBuffer.position(newPosition + Short.BYTES);

            // write the new name
            for (int i = 0; i < newNameByteSize; ++i) {
                byteBuffer.put(i + byteBuffer.position(), newName.getBytes()[i]);
            }
            // update the position after writing the new name in the new buffer
            byteBuffer.position(byteBuffer.position() + newNameByteSize);
            // update the old buffer index to copy from
            oldBufferIndexToCopyFrom = utf8Entry.getEndPosition();
            cpIndexToConstantUtf8.put(
                    utf8Entry.constantPoolIndex,
                    new Constant_Utf8((short) newNameByteSize, newName, newPosition, utf8Entry.constantPoolIndex));
        }
        // copy the remaining bytes from the old buffer to the new buffer
        byteBuffer.put(buf.array(), oldBufferIndexToCopyFrom, oldBufferSize - oldBufferIndexToCopyFrom);

        this.bytecode = byteBuffer.array();
    }
}
