package io.github.algomaster99.terminator.commons.fingerprint.classfile;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

class HashComputerTest {

    private static final Path CLASSFILE = Path.of("src/test/resources/classfile");

    private static final Path PROXY_9 = CLASSFILE
            .resolve("proxy-class-with-incorrect-method-mapping")
            .resolve("$ProxyRuntimeProxy_CommandLine$Command_9.class");

    private static final Path PROXY_13 = CLASSFILE
            .resolve("proxy-class-with-incorrect-method-mapping")
            .resolve("$ProxyIndexProxy_CommandLine$Command_13.class");

    private static final Path FOO = CLASSFILE.resolve("fooToBar").resolve("Foo.class");

    @Test
    void proxiesThatOnlyDifferInFieldNumberingHaveTheSameHash() throws IOException {
        assertThat(HashComputer.computeHash(Files.readAllBytes(PROXY_9)))
                .isEqualTo(HashComputer.computeHash(Files.readAllBytes(PROXY_13)));
    }

    @Test
    void proxiesThatOnlyDifferInMethodAndInitializerOrderHaveTheSameHash() throws IOException {
        // arrange
        byte[] original = Files.readAllBytes(PROXY_9);

        // act
        byte[] permuted = transform(original, HashComputerTest::permuteProxy);

        // assert
        assertThat(permuted).isNotEqualTo(original);
        assertThat(HashComputer.computeHash(permuted)).isEqualTo(HashComputer.computeHash(original));
    }

    @Test
    void changingAnOpcodeChangesTheHash() throws IOException {
        // arrange
        byte[] original = Files.readAllBytes(FOO);

        // act
        byte[] tampered = transform(
                original,
                classNode -> replaceFirst(method(classNode, "sum"), Opcodes.IADD, () -> new InsnNode(Opcodes.ISUB)));

        // assert
        assertThat(HashComputer.computeHash(tampered)).isNotEqualTo(HashComputer.computeHash(original));
    }

    @Test
    void changingAnIntegerConstantChangesTheHash() throws IOException {
        // arrange
        byte[] original = Files.readAllBytes(FOO);

        // act
        byte[] tampered = transform(
                original,
                classNode -> replaceFirst(
                        method(classNode, "<clinit>"), Opcodes.ICONST_1, () -> new InsnNode(Opcodes.ICONST_3)));

        // assert
        assertThat(HashComputer.computeHash(tampered)).isNotEqualTo(HashComputer.computeHash(original));
    }

    @Test
    void changingAReferencedClassChangesTheHash() throws IOException {
        // arrange
        byte[] original = Files.readAllBytes(PROXY_9);

        // act
        byte[] tampered = transform(original, classNode -> {
            for (AbstractInsnNode insn : method(classNode, "<clinit>").instructions) {
                if (insn.getOpcode() == Opcodes.NEW
                        && ((TypeInsnNode) insn).desc.equals("java/lang/NoSuchMethodError")) {
                    ((TypeInsnNode) insn).desc = "java/lang/NoSuchFieldError";
                }
            }
        });

        // assert
        assertThat(HashComputer.computeHash(tampered)).isNotEqualTo(HashComputer.computeHash(original));
    }

    /**
     * Mimics what the JDK may do between two runs: renumber the {@code Method} fields, and reorder the methods and the
     * blocks of {@code <clinit>} that initialize the fields.
     */
    private static void permuteProxy(ClassNode classNode) {
        Map<String, String> newNames = new HashMap<>();
        for (int i = 0; i < classNode.fields.size(); i++) {
            FieldNode field = classNode.fields.get(i);
            String newName = "m" + (classNode.fields.size() - i);
            newNames.put(field.name, newName);
            field.name = newName;
        }
        for (MethodNode method : classNode.methods) {
            for (AbstractInsnNode insn : method.instructions) {
                if (insn instanceof FieldInsnNode && ((FieldInsnNode) insn).owner.equals(classNode.name)) {
                    FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                    fieldInsn.name = newNames.getOrDefault(fieldInsn.name, fieldInsn.name);
                }
            }
        }
        Collections.reverse(classNode.fields);
        Collections.reverse(classNode.methods);

        InsnList clinit = method(classNode, "<clinit>").instructions;
        List<AbstractInsnNode> prefix = new ArrayList<>();
        List<List<AbstractInsnNode>> blocks = new ArrayList<>();
        List<AbstractInsnNode> current = new ArrayList<>();
        for (AbstractInsnNode insn : clinit) {
            if (blocks.isEmpty() && current.isEmpty() && insn.getOpcode() < 0) {
                prefix.add(insn);
                continue;
            }
            current.add(insn);
            if (insn.getOpcode() == Opcodes.PUTSTATIC) {
                blocks.add(current);
                current = new ArrayList<>();
            }
        }
        Collections.reverse(blocks);
        clinit.clear();
        prefix.forEach(clinit::add);
        blocks.forEach(block -> block.forEach(clinit::add));
        current.forEach(clinit::add);
    }

    private static void replaceFirst(
            MethodNode method, int opcode, java.util.function.Supplier<AbstractInsnNode> replacement) {
        for (AbstractInsnNode insn : method.instructions) {
            if (insn.getOpcode() == opcode) {
                method.instructions.set(insn, replacement.get());
                return;
            }
        }
        throw new AssertionError("No instruction with opcode " + opcode + " in " + method.name);
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
