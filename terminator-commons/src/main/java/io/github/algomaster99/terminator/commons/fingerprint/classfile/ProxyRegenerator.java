package io.github.algomaster99.terminator.commons.fingerprint.classfile;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.objectweb.asm.ClassReader;

/**
 * Regenerates proxy classes with the proxy generator of the running JDK.
 *
 * <p>A proxy class has no code of its own: the JDK generates it from the interfaces it implements, and every method
 * forwards to an {@link java.lang.reflect.InvocationHandler}. Its bytecode differs between JDK versions, but on a given
 * JDK it only depends on its interfaces. So a class that claims to be a proxy can be checked by regenerating it,
 * instead of by looking up a checksum recorded on some JDK.
 */
public class ProxyRegenerator {
    private static final String GENERATOR = "java.lang.reflect.ProxyGenerator";

    private ProxyRegenerator() {}

    /**
     * Opens the package of the JDK's proxy generator, which is internal to {@code java.base}, to this class. Must be
     * called before {@link #isGeneratedByJdk(ClassLoader, byte[])}.
     */
    public static void openGenerator(Instrumentation inst) {
        inst.redefineModule(
                Proxy.class.getModule(),
                Set.of(),
                Map.of(),
                Map.of("java.lang.reflect", Set.of(ProxyRegenerator.class.getModule())),
                Set.of(),
                Map.of());
    }

    /**
     * Returns whether the running JDK generates the same class for the interfaces that the given proxy class
     * implements. Returns {@code false} if the class cannot be regenerated, for example because one of its interfaces
     * cannot be loaded.
     */
    public static boolean isGeneratedByJdk(ClassLoader loader, byte[] classfile) {
        try {
            return HashComputer.computeHash(regenerate(loader, classfile)).equals(HashComputer.computeHash(classfile));
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            return false;
        }
    }

    static byte[] regenerate(ClassLoader loader, byte[] classfile) throws ReflectiveOperationException {
        ClassReader reader = new ClassReader(classfile);
        String name = reader.getClassName().replace('/', '.');
        // Keep the declared order because the generator uses it to pick the interface of duplicate methods.
        List<Class<?>> interfaces = new ArrayList<>();
        for (String i : reader.getInterfaces()) {
            interfaces.add(Class.forName(i.replace('/', '.'), false, loader));
        }
        int accessFlags = reader.getAccess();

        Class<?> generator = Class.forName(GENERATOR);
        try {
            // JDK 17 and later
            Method generate = generator.getDeclaredMethod(
                    "generateProxyClass", ClassLoader.class, String.class, List.class, int.class);
            generate.setAccessible(true);
            return (byte[]) generate.invoke(null, loader, name, interfaces, accessFlags);
        } catch (NoSuchMethodException e) {
            // JDK 11
            Method generate = generator.getDeclaredMethod("generateProxyClass", String.class, Class[].class, int.class);
            generate.setAccessible(true);
            return (byte[]) generate.invoke(null, name, interfaces.toArray(new Class<?>[0]), accessFlags);
        }
    }
}
