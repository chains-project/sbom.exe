package io.github.algomaster99.terminator.commons.fingerprint.classfile;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Regenerates {@code jdk.internal.reflect.GeneratedConstructorAccessor*} classes with the accessor generator of the
 * running JDK (up to JDK 17; since JDK 18, JEP 416, reflection uses method handles and no accessors are generated).
 *
 * <p>After a constructor is invoked reflectively enough times, the JDK generates a bytecode accessor for it. The
 * accessor depends only on the constructor it wraps and the JDK, so it could be checked by regenerating it like a proxy
 * or a species. But the generator, {@code MethodAccessorGenerator}, has no entry point that returns the bytecode: it
 * defines the class through {@code ClassDefiner.defineClass}. And a class defined as a side effect of a running
 * transformer is not itself passed to transformers, so the regenerated accessor cannot be captured there.
 *
 * <p>Instead, this class instruments {@code ClassDefiner.defineClass} to hand its {@code byte[]} argument to a capture
 * holder on the bootstrap classpath. During {@link #isGeneratedByJdk}, the generator is invoked, its accessor flows
 * through the instrumented {@code defineClass}, and its bytecode is captured and compared against the original. The
 * regenerated accessor wraps the same constructor, so the two differ only in the number in their name, which
 * {@link HashComputer} ignores.
 */
public class AccessorRegenerator {
    private static final String HOLDER = "io/github/algomaster99/boot/AccessorCapture";
    private static final String GENERATOR = "jdk.internal.reflect.MethodAccessorGenerator";
    private static final String CLASS_DEFINER = "jdk/internal/reflect/ClassDefiner";

    private static final Object LOCK = new Object();

    private static volatile boolean enabled = false;
    private static Field armedThread;
    private static Field captured;

    private AccessorRegenerator() {}

    /**
     * Installs the capture holder on the bootstrap classpath, opens the JDK's accessor generator to this class, and
     * instruments {@code ClassDefiner.defineClass}. Must be called before {@link #isGeneratedByJdk}. Accessor
     * regeneration stays disabled if any step fails, for example on a JDK without this generator.
     */
    public static void openGenerator(Instrumentation inst) {
        try {
            Path jar = Files.createTempFile("accessor-capture", ".jar");
            jar.toFile().deleteOnExit();
            try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jar))) {
                out.putNextEntry(new JarEntry(HOLDER + ".class"));
                out.write(holderClass());
                out.closeEntry();
            }
            inst.appendToBootstrapClassLoaderSearch(new JarFile(jar.toFile()));
            Class<?> holder = Class.forName(HOLDER.replace('/', '.'), true, null);

            // java.base must read the holder's module to call it from ClassDefiner, and open its reflect package to us.
            inst.redefineModule(
                    Object.class.getModule(),
                    Set.of(holder.getModule()),
                    Map.of(),
                    Map.of("jdk.internal.reflect", Set.of(AccessorRegenerator.class.getModule())),
                    Set.of(),
                    Map.of());

            armedThread = holder.getField("armedThread");
            captured = holder.getField("captured");

            inst.addTransformer(
                    new ClassFileTransformer() {
                        @Override
                        public byte[] transform(
                                ClassLoader loader,
                                String className,
                                Class<?> classBeingRedefined,
                                ProtectionDomain protectionDomain,
                                byte[] classfileBuffer) {
                            return CLASS_DEFINER.equals(className) ? instrumentDefineClass(classfileBuffer) : null;
                        }
                    },
                    true);
            for (Class<?> loaded : inst.getAllLoadedClasses()) {
                if (loaded.getName().equals(CLASS_DEFINER.replace('/', '.'))) {
                    inst.retransformClasses(loaded);
                }
            }
            enabled = true;
        } catch (Exception | LinkageError e) {
            System.err.println("[WARN] accessor regeneration disabled: " + e);
        }
    }

    /**
     * Returns whether the running JDK generates the same accessor for the constructor that the given accessor wraps.
     * Returns {@code false} if regeneration is disabled or the accessor cannot be regenerated.
     */
    public static boolean isGeneratedByJdk(ClassLoader loader, byte[] classfile) {
        if (!enabled) {
            return false;
        }
        try {
            Constructor<?> constructor = targetConstructor(loader, classfile);
            byte[] regenerated;
            // The holder keeps a single captured accessor, so only one regeneration may run at a time.
            synchronized (LOCK) {
                armedThread.set(null, Thread.currentThread());
                captured.set(null, null);
                try {
                    regenerate(constructor);
                } finally {
                    armedThread.set(null, null);
                }
                regenerated = (byte[]) captured.get(null);
                captured.set(null, null);
            }
            return regenerated != null
                    && HashComputer.computeHash(classfile).equals(HashComputer.computeHash(regenerated));
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            return false;
        }
    }

    private static Constructor<?> targetConstructor(ClassLoader loader, byte[] classfile)
            throws ReflectiveOperationException {
        String target = ClassFileUtilities.getClassForWhichGeneratedAccessorIsFor(classfile);
        String descriptor = ClassFileUtilities.getConstructorDescriptorFromAccessor(classfile, target);
        Class<?> targetClass = Class.forName(target.replace('/', '.'), false, loader);
        for (Constructor<?> candidate : targetClass.getDeclaredConstructors()) {
            if (Type.getConstructorDescriptor(candidate).equals(descriptor)) {
                return candidate;
            }
        }
        throw new NoSuchMethodException("No constructor " + descriptor + " in " + target);
    }

    private static void regenerate(Constructor<?> constructor) throws ReflectiveOperationException {
        Class<?> generatorClass = Class.forName(GENERATOR);
        Constructor<?> generatorConstructor = generatorClass.getDeclaredConstructor();
        generatorConstructor.setAccessible(true);
        Object generator = generatorConstructor.newInstance();
        Method generate = generatorClass.getDeclaredMethod(
                "generateConstructor", Class.class, Class[].class, Class[].class, int.class);
        generate.setAccessible(true);
        // Defines the accessor through the instrumented ClassDefiner.defineClass, which captures its bytecode.
        generate.invoke(
                generator,
                constructor.getDeclaringClass(),
                constructor.getParameterTypes(),
                constructor.getExceptionTypes(),
                constructor.getModifiers());
    }

    /** Rewrites {@code ClassDefiner.defineClass} to pass its {@code byte[]} argument to the capture holder. */
    private static byte[] instrumentDefineClass(byte[] classfile) {
        ClassReader reader = new ClassReader(classfile);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        reader.accept(
                new ClassVisitor(Opcodes.ASM9, writer) {
                    @Override
                    public MethodVisitor visitMethod(
                            int access, String name, String descriptor, String signature, String[] exceptions) {
                        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                        if (!name.equals("defineClass")) {
                            return mv;
                        }
                        return new MethodVisitor(Opcodes.ASM9, mv) {
                            @Override
                            public void visitCode() {
                                super.visitCode();
                                mv.visitVarInsn(Opcodes.ALOAD, 1); // the byte[] argument
                                mv.visitMethodInsn(Opcodes.INVOKESTATIC, HOLDER, "onDefine", "([B)V", false);
                            }
                        };
                    }
                },
                0);
        return writer.toByteArray();
    }

    /**
     * Generates the capture holder, a bootstrap class {@code ClassDefiner} can call: {@code onDefine} stores the
     * accessor only on the thread that is regenerating, so accessors that other threads define meanwhile are ignored.
     */
    private static byte[] holderClass() {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(
                Opcodes.V1_8,
                Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_SUPER,
                HOLDER,
                null,
                "java/lang/Object",
                null);
        cw.visitField(
                        Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE,
                        "armedThread",
                        "Ljava/lang/Thread;",
                        null,
                        null)
                .visitEnd();
        cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE, "captured", "[B", null, null)
                .visitEnd();
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "onDefine", "([B)V", null, null);
        mv.visitCode();
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;", false);
        mv.visitFieldInsn(Opcodes.GETSTATIC, HOLDER, "armedThread", "Ljava/lang/Thread;");
        Label end = new Label();
        mv.visitJumpInsn(Opcodes.IF_ACMPNE, end);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.PUTSTATIC, HOLDER, "captured", "[B");
        mv.visitLabel(end);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(2, 1);
        mv.visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }
}
