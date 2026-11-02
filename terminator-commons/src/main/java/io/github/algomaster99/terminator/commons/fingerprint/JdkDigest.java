package io.github.algomaster99.terminator.commons.fingerprint;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Computes a digest of a whole JDK, so that the BOMI allowlists a JDK build instead of each of its classes.
 *
 * <p>The digest covers every file of the JDK except the configuration in {@code conf/}, which users edit and
 * distributions replace, and the files that the JDK never runs: documentation, headers, the inputs of jlink, and the
 * sources ({@code legal/}, {@code man/}, {@code include/}, {@code jmods/}, {@code lib/src.zip}). It thus covers the
 * module image, the native libraries, the CDS archives, and the executables, like {@code lib/jspawnhelper}, which
 * the JVM runs to start processes. Listing what to leave out instead of what to keep covers new kinds of files by
 * default. The digest is the hash of a manifest that lists, sorted by path, each file's path relative to the JDK and
 * the hash of its content.
 */
public final class JdkDigest {

    /** The BOMI key of the allowlisted JDKs. Internal class names cannot contain a dot, so no class has this name. */
    public static final String BOMI_KEY = "jdk.digest";

    private static final Set<String> SKIPPED_DIRECTORIES = Set.of("conf", "legal", "man", "include", "jmods");

    private static final Set<String> SKIPPED_FILES = Set.of("lib/src.zip");

    private JdkDigest() {}

    /**
     * Computes the digest of the JDK at {@code javaHome}.
     *
     * @param javaHome  the root of the JDK, like the value of the {@code java.home} property
     * @param algorithm  the name of a {@link MessageDigest} algorithm
     * @return  the digest as a hex string
     */
    public static String compute(Path javaHome, String algorithm) {
        StringBuilder manifest = new StringBuilder();
        for (Path file : filesToDigest(javaHome)) {
            manifest.append(relativePath(javaHome, file))
                    .append('\0')
                    .append(hashFile(file, algorithm))
                    .append('\n');
        }
        return toHex(newDigest(algorithm).digest(manifest.toString().getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Returns the runtime version that the JDK at {@code javaHome} declares in its {@code release} file. The version
     * is self-reported and only labels the digest; it is not checked.
     */
    public static String version(Path javaHome) {
        Path release = javaHome.resolve("release");
        if (!Files.isRegularFile(release)) {
            return "unknown";
        }
        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(release)) {
            properties.load(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        String version = properties.getProperty("JAVA_RUNTIME_VERSION", properties.getProperty("JAVA_VERSION"));
        return version == null ? "unknown" : version.replace("\"", "");
    }

    private static List<Path> filesToDigest(Path javaHome) {
        try (Stream<Path> files = Files.walk(javaHome)) {
            return files.filter(Files::isRegularFile)
                    .filter(file -> !isSkipped(relativePath(javaHome, file)))
                    .sorted((a, b) -> relativePath(javaHome, a).compareTo(relativePath(javaHome, b)))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static boolean isSkipped(String relativePath) {
        int slash = relativePath.indexOf('/');
        return (slash > 0 && SKIPPED_DIRECTORIES.contains(relativePath.substring(0, slash)))
                || SKIPPED_FILES.contains(relativePath);
    }

    private static String relativePath(Path javaHome, Path file) {
        return javaHome.relativize(file).toString().replace(File.separatorChar, '/');
    }

    private static String hashFile(Path file, String algorithm) {
        MessageDigest digest = newDigest(algorithm);
        byte[] buffer = new byte[1 << 20];
        try (InputStream in = Files.newInputStream(file)) {
            int read;
            while ((read = in.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return toHex(digest.digest());
    }

    private static MessageDigest newDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Unknown digest algorithm: " + algorithm, e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }
}
