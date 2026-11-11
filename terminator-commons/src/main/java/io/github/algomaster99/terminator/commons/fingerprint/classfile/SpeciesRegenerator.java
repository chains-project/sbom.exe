package io.github.algomaster99.terminator.commons.fingerprint.classfile;

import java.lang.instrument.Instrumentation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import org.objectweb.asm.ClassReader;

/**
 * Regenerates {@code java.lang.invoke.BoundMethodHandle$Species_*} classes with the species generator of the running
 * JDK.
 *
 * <p>A species class is the storage carrier for a bound method handle: the JDK generates a concrete subclass of
 * {@code java.lang.invoke.BoundMethodHandle} whose fields match the shape of the captured arguments. The shape is
 * encoded in the class name, for example {@code Species_LLLII} has three reference fields and two int fields. Only a
 * fixed set of shapes is baked into {@code java.base}; the rest are spun lazily at runtime, so they are not in the
 * BOMI. Like a proxy, a species is fully determined by its name and the running JDK, so a class that claims to be a
 * species can be checked by regenerating it, instead of by looking up a checksum recorded on some JDK.
 */
public class SpeciesRegenerator {
    private static final String CLASS_SPECIALIZER = "java.lang.invoke.ClassSpecializer";
    private static final String SPECIES_DATA = "java.lang.invoke.ClassSpecializer$SpeciesData";
    private static final String SPECIES_INFIX = "$Species_";

    // The species generator of the running JDK. openGenerator resolves it before the transformer is registered.
    private static Object specializer;
    private static Object speciesFactory;
    // (Object specializer, Object key) -> Object speciesData
    private static MethodHandle newSpeciesData;
    // (Object factory, String className, Object speciesData) -> byte[]
    private static MethodHandle generateConcreteSpeciesCodeFile;

    private SpeciesRegenerator() {}

    /**
     * Opens the package of the JDK's species generator to this class and resolves the generator. Call it before
     * registering the transformer that calls {@link #isGeneratedByJdk(byte[])}.
     *
     * <p>A check must not spin method handles. They can need the species that the JDK is defining, which the JDK then
     * defines twice. Reflection spins method handles on its first call since JDK 18. So the generator is resolved here
     * into method handles, which a check calls with {@code invokeExact}.
     */
    public static void openGenerator(Instrumentation inst) {
        inst.redefineModule(
                MethodHandle.class.getModule(),
                Set.of(),
                Map.of(),
                Map.of("java.lang.invoke", Set.of(SpeciesRegenerator.class.getModule())),
                Set.of(),
                Map.of());
        try {
            resolveGenerator();
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            // isGeneratedByJdk tries again and returns false if it fails.
        }
    }

    /**
     * Returns whether the running JDK generates the same class for the shape that the given species class encodes in
     * its name. Returns {@code false} if the class cannot be regenerated.
     */
    public static boolean isGeneratedByJdk(byte[] classfile) {
        try {
            return HashComputer.computeHash(regenerate(classfile)).equals(HashComputer.computeHash(classfile));
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            return false;
        }
    }

    static byte[] regenerate(byte[] classfile) throws ReflectiveOperationException {
        String internalName =
                new ClassReader(classfile).getClassName(); // java/lang/invoke/BoundMethodHandle$Species_LLLII
        String key = internalName.substring(internalName.indexOf(SPECIES_INFIX) + SPECIES_INFIX.length());
        resolveGenerator();
        try {
            // newSpeciesData builds the metadata of the species without generating or loading its class, unlike
            // findSpecies. So regenerating a species does not re-enter the transformer for that same species.
            Object speciesData = (Object) newSpeciesData.invokeExact(specializer, (Object) key);
            return (byte[]) generateConcreteSpeciesCodeFile.invokeExact(
                    speciesFactory, internalName.replace('/', '.'), speciesData);
        } catch (Throwable t) {
            // Wrap every failure like Method.invoke does. Then the check fails closed.
            throw new InvocationTargetException(t);
        }
    }

    private static synchronized void resolveGenerator() throws ReflectiveOperationException {
        if (generateConcreteSpeciesCodeFile != null) {
            return;
        }
        Class<?> boundMethodHandle = Class.forName("java.lang.invoke.BoundMethodHandle");
        Field specializerField = boundMethodHandle.getDeclaredField("SPECIALIZER");
        specializerField.setAccessible(true);
        specializer = specializerField.get(null);
        Class<?> classSpecializer = Class.forName(CLASS_SPECIALIZER);
        Method factory = classSpecializer.getDeclaredMethod("factory");
        factory.setAccessible(true);
        speciesFactory = factory.invoke(specializer);
        Method speciesDataOf = classSpecializer.getDeclaredMethod("newSpeciesData", Object.class);
        speciesDataOf.setAccessible(true);
        // generateConcreteSpeciesCodeFile is declared on ClassSpecializer.Factory, the superclass of the concrete
        // factory that the specializer returns.
        Method generate = speciesFactory
                .getClass()
                .getSuperclass()
                .getDeclaredMethod("generateConcreteSpeciesCodeFile", String.class, Class.forName(SPECIES_DATA));
        generate.setAccessible(true);
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        newSpeciesData =
                lookup.unreflect(speciesDataOf).asType(MethodType.methodType(Object.class, Object.class, Object.class));
        generateConcreteSpeciesCodeFile = lookup.unreflect(generate)
                .asType(MethodType.methodType(byte[].class, Object.class, String.class, Object.class));
    }
}
