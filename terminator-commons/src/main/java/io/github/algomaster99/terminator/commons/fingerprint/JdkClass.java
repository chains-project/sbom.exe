package io.github.algomaster99.terminator.commons.fingerprint;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * A class that represents a JDK class. It contains the name of the class and the bytes of the class as a {@link ByteBuffer}.
 */
public final class JdkClass {
    private final String name;
    private final ByteBuffer bytes;

    /**
     *
     */
    public JdkClass(String name, ByteBuffer bytes) {
        this.name = name;
        this.bytes = bytes;
    }

    public String name() {
        return name;
    }

    public byte[] bytes() {
        return getBytesFromBuffer(bytes);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (JdkClass) obj;
        return Objects.equals(this.name, that.name) && Objects.equals(this.bytes, that.bytes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, bytes);
    }

    @Override
    public String toString() {
        return "JdkClass[" + "name=" + name + ", " + "bytes=" + bytes + ']';
    }

    /**
     * Converts a bytebuffer to a byte array. If the buffer has an array, it returns it, otherwise it copies the bytes. This is needed because the buffer is not guaranteed to have an array.
     * See {@link java.nio.ByteBuffer#hasArray()} and {link java.nio.DirectByteBuffer}.
     * @param buffer  the buffer to convert
     * @return  the byte array
     */
    private static byte[] getBytesFromBuffer(ByteBuffer buffer) {
        if (buffer.hasArray()) {
            return buffer.array();
        }
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }
}
