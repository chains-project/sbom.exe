package io.github.algomaster99.terminator.commons.fingerprint.provenance;

import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileAttributes;

/**
 * Records provenance of jar file.
 */
public record Jar(ClassFileAttributes classFileAttributes, String path) implements Provenance {}
