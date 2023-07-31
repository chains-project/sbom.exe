package io.github.algomaster99.terminator.commons.fingerprint;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Records provenance of maven package.
 */
@JsonDeserialize(as = Maven.class)
public record Maven(
        String groupId,
        String artifactId,
        String version,
        String className,
        String classFileVersion,
        String hash,
        String algorithm)
        implements Fingerprint {}
