package io.github.algomaster99.terminator.commons.fingerprint.provenance;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileAttributes;

/**
 * Records provenance of maven package.
 */
@JsonDeserialize(as = Maven.class)
public record Maven(ClassFileAttributes classFileAttributes, String groupId, String artifactId, String version)
        implements Provenance {}
