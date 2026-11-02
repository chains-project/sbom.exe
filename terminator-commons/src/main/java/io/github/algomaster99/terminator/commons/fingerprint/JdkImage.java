package io.github.algomaster99.terminator.commons.fingerprint;

import java.io.IOException;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

/**
 * Reads classes from the module image ({@code lib/modules}) of the running JDK, which {@link JdkDigest} covers.
 */
public final class JdkImage {

    private static final FileSystem IMAGE = FileSystems.getFileSystem(URI.create("jrt:/"));

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
        // No string concatenation: it compiles to an invokedynamic, which can spin classes while a transformer runs.
        try {
            return Files.readAllBytes(IMAGE.getPath("/modules", module.getName(), className.concat(".class")));
        } catch (IOException e) {
            return null;
        }
    }
}
