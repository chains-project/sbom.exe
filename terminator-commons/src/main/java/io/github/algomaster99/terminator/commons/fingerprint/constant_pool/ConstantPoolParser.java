package io.github.algomaster99.terminator.commons.fingerprint.constant_pool;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

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

    private static final int OXF0 = 0xf0;

    private static final int OXE0 = 0xe0;

    private static final int OX3F = 0x3F;

    private ConstantPoolParser() {
        throw new IllegalStateException("Utility class");
    }

    public static byte[] rewriteThisClass(byte[] b, String newName) {
        return parseConstantPoolClassReferences(ByteBuffer.wrap(b), newName);
    }

    private static byte[] parseConstantPoolClassReferences(ByteBuffer buf, String newName) {
        if (buf.order(ByteOrder.BIG_ENDIAN).getInt() != HEAD) {
            throw new RuntimeException("Not a valid classfile");
        }
        Map<Short, Constant_Class> cpIndexToConstantClass = new HashMap<>();
        Map<Short, Constant_Utf8> cpIndexToConstantUtf8 = new HashMap<>();
        buf.getChar();
        buf.getChar(); // minor + ver
        for (int ix = 1, num = buf.getShort(); ix < num; ix++) {
            byte tag = buf.get();
            int startPosition = buf.position();
            switch (tag) {
                case CONSTANT_UTF8:
                    cpIndexToConstantUtf8.put((short) ix, createUtf8Entry(buf, startPosition));
                    break;
                case CONSTANT_CLASS:
                    cpIndexToConstantClass.put((short) ix, new Constant_Class(buf.getShort(), startPosition));
                    break;
                case CONSTANT_STRING:
                case CONSTANT_METHOD_TYPE:
                    buf.getChar();
                    break;
                case CONSTANT_FIELDREF:
                case CONSTANT_METHODREF:
                case CONSTANT_INTERFACEMETHODREF:
                    buf.getShort(); // gets the class index
                    buf.getChar();
                    break;
                case CONSTANT_NAME_AND_TYPE:
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
        buf.getChar(); // access flags
        short cpIndexOfThisClass = buf.getShort(); // this class
        buf.rewind();

        Constant_Class thisClass = cpIndexToConstantClass.get(cpIndexOfThisClass);
        Constant_Utf8 utf8OfThisClass = cpIndexToConstantUtf8.get(thisClass.classIndex);

        int newNameByteSize = newName.getBytes(StandardCharsets.UTF_8).length;
        int oldBufferSize = buf.limit();
        int newBufferSize = oldBufferSize - utf8OfThisClass.length + newNameByteSize;

        ByteBuffer byteBuffer = ByteBuffer.allocate(newBufferSize);
        byteBuffer.put(buf.array(), 0, utf8OfThisClass.startPosition);
        byteBuffer.putShort(utf8OfThisClass.startPosition, (short) newNameByteSize);
        byteBuffer.position(utf8OfThisClass.startPosition + newNameByteSize);
        for (int i = 0; i < newName.getBytes().length; ++i) {
            byteBuffer.put(i + utf8OfThisClass.startPosition + 2, newName.getBytes()[i]);
        }
        byteBuffer.position(utf8OfThisClass.startPosition + 2 + newName.getBytes().length);
        byteBuffer.put(buf.array(), utf8OfThisClass.getEndPosition(), oldBufferSize - utf8OfThisClass.getEndPosition());

        return byteBuffer.array();
    }

    private static Constant_Utf8 createUtf8Entry(ByteBuffer buf, int startPosition) {
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

        return new Constant_Utf8((short) size, utf8Value, startPosition);
    }
}
