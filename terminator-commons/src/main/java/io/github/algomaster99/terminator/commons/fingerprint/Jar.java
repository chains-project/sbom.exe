package io.github.algomaster99.terminator.commons.fingerprint;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Records provenance of jar file.
 */
@JsonDeserialize(as = Jar.class)
public record Jar(String path, String className, String classFileVersion, String hash, String algorithm)
        implements Fingerprint {}
