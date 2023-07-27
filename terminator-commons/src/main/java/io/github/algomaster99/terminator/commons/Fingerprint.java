package io.github.algomaster99.terminator.commons;

public record Fingerprint(
        String groupId,
        String artifactId,
        String version,
        String className,
        String classFileVersion,
        String hash,
        String algorithm) {}
