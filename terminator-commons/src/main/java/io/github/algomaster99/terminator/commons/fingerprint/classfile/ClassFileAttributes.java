package io.github.algomaster99.terminator.commons.fingerprint.classfile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public final class ClassFileAttributes {
    private final String classfileVersion;
    private final String hash;
    private final String algorithm;

    @JsonCreator
    public ClassFileAttributes(
            @JsonProperty("classfileVersion") String classfileVersion,
            @JsonProperty("hash") String hash,
            @JsonProperty("algorithm") String algorithm) {
        this.classfileVersion = classfileVersion;
        this.hash = hash;
        this.algorithm = algorithm;
    }

    @JsonGetter("classfileVersion")
    public String classfileVersion() {
        return classfileVersion;
    }

    @JsonGetter("hash")
    public String hash() {
        return hash;
    }

    @JsonGetter("algorithm")
    public String algorithm() {
        return algorithm;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ClassFileAttributes) obj;
        return Objects.equals(this.classfileVersion, that.classfileVersion)
                && Objects.equals(this.hash, that.hash)
                && Objects.equals(this.algorithm, that.algorithm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classfileVersion, hash, algorithm);
    }

    @Override
    public String toString() {
        return "ClassFileAttributes[" + "classfileVersion="
                + classfileVersion + ", " + "hash="
                + hash + ", " + "algorithm="
                + algorithm + ']';
    }
}
