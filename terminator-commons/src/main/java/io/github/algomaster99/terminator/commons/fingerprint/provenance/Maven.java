package io.github.algomaster99.terminator.commons.fingerprint.provenance;

import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileAttributes;

/**
 * Records provenance of maven package.
 */
public record Maven(ClassFileAttributes classFileAttributes, String groupId, String artifactId, String version)
        implements Provenance {}
