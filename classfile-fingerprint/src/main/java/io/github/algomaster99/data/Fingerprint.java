package io.github.algomaster99.data;

public record Fingerprint(
        String groupId, String artifactId, String version, String className, String hash, String algorithm) {}
