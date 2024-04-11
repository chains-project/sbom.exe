package io.github.algomaster99.terminator.commons.fingerprint.classfile;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.algomaster99.terminator.commons.fingerprint.JdkClass;
import io.github.algomaster99.terminator.commons.fingerprint.JdkClassFinder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
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
        void isSyntheticClass_true() throws IOException {
            Path synthetic = CLASSFILE.resolve("$Proxy39.class");

            byte[] unmodifiedBytes = Files.readAllBytes(synthetic);
            assertThat(RuntimeClass.isSynthetic(unmodifiedBytes)).isFalse();

            byte[] modifiedBytes = makeClassfileSynthetic(Files.readAllBytes(synthetic));
            assertThat(RuntimeClass.isSynthetic(modifiedBytes)).isTrue();
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

    @EnabledIfSystemProperty(named = "java.vendor.version", matches = "Temurin-17\\.0\\.10\\+7")
    @Test
    void jdk17_0_10_bytecodeHashShouldRemainSame(@TempDir Path tempDir) throws IOException {
        // arrange
        List<JdkClass> jdkClasses = JdkClassFinder.listJdkClasses();

        jdkClasses.stream().forEach(jdkClass -> {
            String expectedHash = null;
            expectedHash = HashComputer.computeHash(jdkClass.bytes());

            ClassReader reader = new ClassReader(jdkClass.bytes());
            ClassWriter writer = new ClassWriter(reader, 0);
            reader.accept(writer, 0);

            byte[] modifiedBytesButShouldNotHaveBeen = writer.toByteArray();
            String actualHash = null;
            actualHash = HashComputer.computeHash(modifiedBytesButShouldNotHaveBeen);

            assertThat(actualHash).isEqualTo(expectedHash);
        });
    }
}
