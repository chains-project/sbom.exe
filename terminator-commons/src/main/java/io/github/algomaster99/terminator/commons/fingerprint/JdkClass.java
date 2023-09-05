package io.github.algomaster99.terminator.commons.fingerprint;

import java.nio.ByteBuffer;

/**
 * A class that represents a JDK class. It contains the name of the class and the bytes of the class as a {@link ByteBuffer}.
 */
public record JdkClass(String name, ByteBuffer bytes) {}
