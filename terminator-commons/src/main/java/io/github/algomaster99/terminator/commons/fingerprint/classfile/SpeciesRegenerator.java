package io.github.algomaster99.terminator.commons.fingerprint.classfile;

import java.lang.instrument.Instrumentation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
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

    private SpeciesRegenerator() {}

    /**
     * Opens the package of the JDK's species generator, which is internal to {@code java.base}, to this class. Must be
     * called before {@link #isGeneratedByJdk(byte[])}.
     */
    public static void openGenerator(Instrumentation inst) {
        inst.redefineModule(
                MethodHandle.class.getModule(),
                Set.of(),
                Map.of(),
                Map.of("java.lang.invoke", Set.of(SpeciesRegenerator.class.getModule())),
                Set.of(),
                Map.of());
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
        ClassReader reader = new ClassReader(classfile);
        String internalName = reader.getClassName(); // java/lang/invoke/BoundMethodHandle$Species_LLLII
        String key = internalName.substring(internalName.indexOf(SPECIES_INFIX) + SPECIES_INFIX.length());
        String className = internalName.replace('/', '.');

        Class<?> boundMethodHandle = Class.forName("java.lang.invoke.BoundMethodHandle");
        Field specializerField = boundMethodHandle.getDeclaredField("SPECIALIZER");
        specializerField.setAccessible(true);
        Object specializer = specializerField.get(null);

        Class<?> classSpecializer = Class.forName(CLASS_SPECIALIZER);
        // newSpeciesData builds the metadata of the species without generating or loading its class, unlike
        // findSpecies. So regenerating a species does not re-enter the transformer for that same species.
        Method newSpeciesData = classSpecializer.getDeclaredMethod("newSpeciesData", Object.class);
        newSpeciesData.setAccessible(true);
        Object speciesData = newSpeciesData.invoke(specializer, key);

        Method factory = classSpecializer.getDeclaredMethod("factory");
        factory.setAccessible(true);
        Object speciesFactory = factory.invoke(specializer);

        // generateConcreteSpeciesCodeFile is declared on ClassSpecializer.Factory, the superclass of the concrete
        // factory that the specializer returns.
        Class<?> speciesDataType = Class.forName(SPECIES_DATA);
        Method generate = speciesFactory
                .getClass()
                .getSuperclass()
                .getDeclaredMethod("generateConcreteSpeciesCodeFile", String.class, speciesDataType);
        generate.setAccessible(true);
        return (byte[]) generate.invoke(speciesFactory, className, speciesData);
    }
}
