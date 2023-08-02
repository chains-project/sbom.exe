package io.github.algomaster99.terminator.commons.fingerprint.provenance;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileAttributes;

/**
 * Records provenance of jar file.
 */
@JsonDeserialize(as = Jar.class)
public record Jar(ClassFileAttributes classFileAttributes, String path) implements Provenance {}
