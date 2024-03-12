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

    public ByteBuffer bytes() {
        return bytes;
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
}
