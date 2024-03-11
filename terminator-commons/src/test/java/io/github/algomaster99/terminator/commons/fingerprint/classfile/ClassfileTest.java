package io.github.algomaster99.terminator.commons.fingerprint.classfile;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.TraceClassVisitor;

public class ClassfileTest {

    private static final Path TEST_RESOURCES = Path.of("src/test/resources");

    private static final Path CLASSFILE = TEST_RESOURCES.resolve("classfile");

    @Nested
    class IsRuntimeGeneratedClass {

        @Test
        void isAProxyClass_true() throws IOException {
            Path proxy39 = CLASSFILE.resolve("$Proxy39.class");
            Path proxy40 = CLASSFILE.resolve("$Proxy40.class");

            assertThat(RuntimeClass.isProxyClass(Files.readAllBytes(proxy39))).isTrue();
            assertThat(RuntimeClass.isProxyClass(Files.readAllBytes(proxy40))).isTrue();
        }

        @Test
        void isProxyClass_false() throws IOException {
            Path maven = CLASSFILE.resolve("Maven.class");

            assertThat(RuntimeClass.isProxyClass(Files.readAllBytes(maven))).isFalse();
        }

        @Test
        void isSyntheticClass_true() throws IOException {
            Path synthetic = CLASSFILE.resolve("$Proxy39.class");

            byte[] unmodifiedBytes = Files.readAllBytes(synthetic);
            assertThat(RuntimeClass.isSynthetic(unmodifiedBytes)).isFalse();

            byte[] modifiedBytes = makeClassfileSynthetic(Files.readAllBytes(synthetic));
            assertThat(RuntimeClass.isSynthetic(modifiedBytes)).isTrue();
        }

        @Test
        void isGeneratedClassExtendingMagicAccessor_true() throws IOException {
            Path generatedConstructorAccessor = CLASSFILE.resolve("GeneratedConstructorAccessor15.class");
            assertThat(RuntimeClass.isGeneratedClassExtendingMagicAccessor(
                            Files.readAllBytes(generatedConstructorAccessor)))
                    .isTrue();

            Path generatedMethodAccessor = CLASSFILE.resolve("GeneratedMethodAccessor1.class");
            assertThat(RuntimeClass.isGeneratedClassExtendingMagicAccessor(Files.readAllBytes(generatedMethodAccessor)))
                    .isTrue();
        }

        private byte[] makeClassfileSynthetic(byte[] classfileBytes) {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(classfileBytes);
            classReader.accept(classNode, 0);
            classNode.access |= Opcodes.ACC_SYNTHETIC;

            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            classNode.accept(new TraceClassVisitor(writer, null));
            return writer.toByteArray();
        }
    }
}
