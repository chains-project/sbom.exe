package io.github.algomaster99.terminator.commons.fingerprint.classfile;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

class ProxyRegeneratorTest {

    public interface Greeter {
        String greet(String name);
    }

    @Test
    void proxyGeneratedByTheJdkIsRegeneratedIdentically(@TempDir Path workingDirectory) throws Exception {
        // arrange
        byte[] proxy = proxyOfGreeterGeneratedInAnotherJvm(workingDirectory);

        // act
        boolean isGeneratedByJdk = ProxyRegenerator.isGeneratedByJdk(Greeter.class.getClassLoader(), proxy);

        // assert
        assertThat(isGeneratedByJdk).isTrue();
    }

    @Test
    void classThatOnlyClaimsToBeAProxyIsNotRegeneratedIdentically(@TempDir Path workingDirectory) throws Exception {
        // arrange
        byte[] proxy = proxyOfGreeterGeneratedInAnotherJvm(workingDirectory);

        // act: an attacker would run their own code when the class is initialized
        byte[] forged = transform(
                proxy,
                classNode -> method(classNode, "<clinit>")
                        .instructions
                        .insert(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/System", "gc", "()V", false)));

        // assert
        assertThat(ClassFileUtilities.isProxyClass(forged)).isTrue();
        assertThat(ProxyRegenerator.isGeneratedByJdk(Greeter.class.getClassLoader(), forged))
                .isFalse();
    }

    @Test
    void proxyOfAnInterfaceThatCannotBeLoadedIsNotGeneratedByJdk() {
        // arrange
        ClassWriter writer = new ClassWriter(0);
        writer.visit(
                Opcodes.V11,
                Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_SUPER,
                "jdk/proxy1/$Proxy99",
                null,
                "java/lang/reflect/Proxy",
                new String[] {"does/not/Exist"});
        writer.visitEnd();

        // act
        boolean isGeneratedByJdk =
                ProxyRegenerator.isGeneratedByJdk(Greeter.class.getClassLoader(), writer.toByteArray());

        // assert
        assertThat(isGeneratedByJdk).isFalse();
    }

    /**
     * Returns the proxy of {@link Greeter} that the JDK running the tests generates. Another JVM creates it, and an
     * agent saves the bytes that the JDK defines, which are the bytes that the watchdog sees.
     */
    private static byte[] proxyOfGreeterGeneratedInAnotherJvm(Path workingDirectory)
            throws IOException, InterruptedException {
        // The agent class is on the class path, so the agent JAR only needs a manifest.
        Path agent = workingDirectory.resolve("save-proxies.jar");
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().putValue("Premain-Class", SaveProxies.class.getName());
        new JarOutputStream(Files.newOutputStream(agent), manifest).close();

        Process process = new ProcessBuilder(
                        Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                        "-javaagent:" + agent + "=" + workingDirectory,
                        "-cp",
                        System.getProperty("java.class.path"),
                        CreateProxyOfGreeter.class.getName())
                .inheritIO()
                .start();
        assertThat(process.waitFor()).isZero();

        // The JDK may generate proxies of its own, for example of annotations.
        String greeter = Type.getInternalName(Greeter.class);
        try (Stream<Path> files = Files.list(workingDirectory)) {
            List<byte[]> proxies = files.filter(f -> f.toString().endsWith(".class"))
                    .map(ProxyRegeneratorTest::readAllBytes)
                    .filter(bytes -> Arrays.asList(new ClassReader(bytes).getInterfaces())
                            .contains(greeter))
                    .collect(Collectors.toList());
            assertThat(proxies).hasSize(1);
            return proxies.get(0);
        }
    }

    public static class SaveProxies implements ClassFileTransformer {
        private final Path outputDirectory;

        private SaveProxies(Path outputDirectory) {
            this.outputDirectory = outputDirectory;
        }

        public static void premain(String outputDirectory, Instrumentation inst) {
            inst.addTransformer(new SaveProxies(Path.of(outputDirectory)));
        }

        @Override
        public byte[] transform(
                ClassLoader loader,
                String className,
                Class<?> classBeingRedefined,
                ProtectionDomain protectionDomain,
                byte[] classfileBuffer) {
            if (className != null && ClassFileUtilities.isProxyClass(classfileBuffer)) {
                try {
                    Files.write(outputDirectory.resolve(className.replace('/', '.') + ".class"), classfileBuffer);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
            return null;
        }
    }

    public static class CreateProxyOfGreeter {
        public static void main(String[] args) {
            Proxy.newProxyInstance(
                    Greeter.class.getClassLoader(), new Class<?>[] {Greeter.class}, (proxy, method, arguments) -> null);
        }
    }

    private static byte[] readAllBytes(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static MethodNode method(ClassNode classNode, String name) {
        return classNode.methods.stream()
                .filter(m -> m.name.equals(name))
                .findFirst()
                .orElseThrow();
    }

    private static byte[] transform(byte[] bytes, Consumer<ClassNode> transformation) {
        ClassNode classNode = new ClassNode();
        new ClassReader(bytes).accept(classNode, 0);
        transformation.accept(classNode);
        ClassWriter writer = new ClassWriter(0);
        classNode.accept(writer);
        return writer.toByteArray();
    }
}
