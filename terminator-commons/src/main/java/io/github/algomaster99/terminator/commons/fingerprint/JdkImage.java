package io.github.algomaster99.terminator.commons.fingerprint;

import java.io.IOException;
import java.io.InputStream;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Reads classes from the module image ({@code lib/modules}) of the running JDK, which {@link JdkDigest} covers.
 */
public final class JdkImage {

    private static final FileSystem IMAGE = FileSystems.getFileSystem(URI.create("jrt:/"));

    private static final Path JRT_FS = Path.of(System.getProperty("java.home"), "lib", "jrt-fs.jar");

    private static final Set<String> MODULES = new HashSet<>();

    static {
        for (ModuleReference module : ModuleFinder.ofSystem().findAll()) {
            MODULES.add(module.descriptor().name());
        }
    }

    private JdkImage() {}

    /**
     * Loads the classes that read the image. Call it before adding a transformer, so that they are not loaded while
     * the transformer handles a class.
     */
    public static void preload() {
        read(Object.class.getModule(), "java/lang/Object");
    }

    /**
     * Returns the bytes that the module image holds for a class of a JDK module.
     *
     * <p>The module tells where in the image to look, because the image stores each class under its module. It also
     * tells a JDK class apart from a class with the same package on the class path or the boot class path, which is
     * in an unnamed module.
     *
     * @param module  the module of the class, as passed to a {@code ClassFileTransformer}
     * @param className  the internal name of the class
     * @return  the bytes, or {@code null} if the module is not a JDK module or the image does not hold the class, like
     *     for a class generated at runtime
     */
    public static byte[] read(Module module, String className) {
        if (module == null || !module.isNamed() || !MODULES.contains(module.getName())) {
            return null;
        }
        // We use an input stream instead of Files.readAllBytes, because the image's input stream holds the bytes in
        // memory. Files.readAllBytes reads through an interruptible channel, so an interrupt fails the read and makes a
        // JDK class look generated.
        // No string concatenation: it compiles to an invokedynamic, which can spin classes while a transformer runs.
        try (InputStream in =
                Files.newInputStream(IMAGE.getPath("/modules", module.getName(), className.concat(".class")))) {
            return in.readAllBytes();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Returns the bytes that {@code lib/jrt-fs.jar} of the running JDK holds for a class.
     *
     * <p>The JDK defines the classes of this JAR in an unnamed module when a program opens the jrt file system of a
     * given {@code java.home}. {@link JdkDigest} covers the JAR, so these classes belong to the JDK. It is the only JAR
     * that the JDK ships outside {@code jmods}. Any other JAR in {@code java.home}, like one added after the digest
     * check, is not trusted.
     *
     * @param protectionDomain  the protection domain of the class, as passed to a {@code ClassFileTransformer}
     * @param className  the internal name of the class
     * @return  the bytes, or {@code null} if the class does not come from {@code lib/jrt-fs.jar}
     */
    public static byte[] readFromJrtFs(ProtectionDomain protectionDomain, String className) {
        CodeSource codeSource = protectionDomain == null ? null : protectionDomain.getCodeSource();
        URL location = codeSource == null ? null : codeSource.getLocation();
        if (location == null || !"file".equals(location.getProtocol())) {
            return null;
        }
        try {
            Path jar = Path.of(location.toURI()).normalize();
            if (!jar.equals(JRT_FS)) {
                return null;
            }
            try (ZipFile zip = new ZipFile(jar.toFile())) {
                ZipEntry entry = zip.getEntry(className.concat(".class"));
                if (entry == null) {
                    return null;
                }
                try (InputStream in = zip.getInputStream(entry)) {
                    return in.readAllBytes();
                }
            }
        } catch (IOException | URISyntaxException | RuntimeException e) {
            return null;
        }
    }
}
