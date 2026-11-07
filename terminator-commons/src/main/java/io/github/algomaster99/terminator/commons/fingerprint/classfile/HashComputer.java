package io.github.algomaster99.terminator.commons.fingerprint.classfile;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.Printer;

public class HashComputer {
    // Not a valid Java identifier, so no compiled class can refer to a class with this name.
    private static final String CANONICAL_CLASS_NAME = "<this>";

    private static final String PROXY_SUPERCLASS = "java/lang/reflect/Proxy";

    private HashComputer() {}

    /**
     * Computes the SHA-256 checksum of a canonical view of the classfile.
     *
     * <p>The canonical view contains the class header, all fields, and all methods including every instruction with
     * its symbolic operands. Debug information and stack map frames are dropped, the name of the class itself is
     * replaced by a constant, and fields and methods are sorted because their declaration order has no effect on
     * behaviour. For proxy classes, the static {@code Method} fields are additionally named after their initializer
     * because the JDK numbers them non-deterministically.
     */
    public static String computeHash(byte[] bytes) {
        return computeHash(bytes, Map.of());
    }

    /**
     * Computes the checksum like {@link #computeHash(byte[])}, but first renames the given classes wherever the
     * classfile refers to them. The accessor that the JDK generates for the constructor of a proxy class refers to the
     * proxy by its numbered name, which depends on how many proxies were created before, so the proxy has to be renamed
     * to its name in the BOMI.
     */
    public static String computeHash(byte[] bytes, Map<String, String> classNames) {
        ClassReader reader = new ClassReader(bytes);
        Map<String, String> mapping = new HashMap<>(classNames);
        mapping.put(reader.getClassName(), CANONICAL_CLASS_NAME);
        ClassNode classNode = new ClassNode();
        reader.accept(
                new ClassRemapper(classNode, new SimpleRemapper(mapping)),
                ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

        boolean isProxy = PROXY_SUPERCLASS.equals(classNode.superName);
        if (isProxy) {
            renameFieldsAfterTheirInitializer(classNode);
        }
        renameLambdaMethods(classNode);
        return toHexString(sha256(canonicalView(classNode, isProxy)));
    }

    private static String canonicalView(ClassNode classNode, boolean isProxy) {
        StringBuilder sb = new StringBuilder();
        String[] interfaces = classNode.interfaces.toArray(new String[0]);
        Arrays.sort(interfaces);
        sb.append("class ")
                .append(classNode.access)
                .append(' ')
                .append(classNode.name)
                .append(" extends ")
                .append(classNode.superName)
                .append(" implements ")
                .append(String.join(",", interfaces))
                .append('\n');

        List<FieldNode> fields = new ArrayList<>(classNode.fields);
        fields.sort(Comparator.comparing((FieldNode f) -> f.name).thenComparing(f -> f.desc));
        for (FieldNode field : fields) {
            sb.append("field ")
                    .append(field.access)
                    .append(' ')
                    .append(field.name)
                    .append(' ')
                    .append(field.desc)
                    .append(" = ")
                    .append(constant(field.value))
                    .append('\n');
        }

        List<MethodNode> methods = new ArrayList<>(classNode.methods);
        methods.sort(Comparator.comparing((MethodNode m) -> m.name).thenComparing(m -> m.desc));
        for (MethodNode method : methods) {
            List<String> exceptions = new ArrayList<>(method.exceptions);
            exceptions.sort(null);
            sb.append("method ")
                    .append(method.access)
                    .append(' ')
                    .append(method.name)
                    .append(method.desc)
                    .append(" throws ")
                    .append(String.join(",", exceptions))
                    .append('\n');

            List<String> instructions = instructions(method);
            if (isProxy && method.name.equals("<clinit>")) {
                instructions = sortInitializers(instructions, classNode.name);
            }
            instructions.forEach(i -> sb.append("  ").append(i).append('\n'));

            Map<LabelNode, Integer> labels = labelPositions(method);
            for (TryCatchBlockNode tryCatch : method.tryCatchBlocks) {
                sb.append("  catch ")
                        .append(tryCatch.type)
                        .append(' ')
                        .append(labels.get(tryCatch.start))
                        .append(' ')
                        .append(labels.get(tryCatch.end))
                        .append(' ')
                        .append(labels.get(tryCatch.handler))
                        .append('\n');
            }
        }
        return sb.toString();
    }

    /**
     * Renames every static field assigned in {@code <clinit>} after the instructions that compute its value. A proxy
     * binds one {@code Method} object per field, so the new name identifies the proxied method instead of its position.
     */
    private static void renameFieldsAfterTheirInitializer(ClassNode classNode) {
        MethodNode clinit = classNode.methods.stream()
                .filter(m -> m.name.equals("<clinit>"))
                .findFirst()
                .orElse(null);
        if (clinit == null) {
            return;
        }
        Map<LabelNode, Integer> labels = labelPositions(clinit);
        Map<String, String> newNames = new HashMap<>();
        StringBuilder initializer = new StringBuilder();
        for (AbstractInsnNode insn : clinit.instructions) {
            if (insn.getOpcode() == Opcodes.PUTSTATIC && ((FieldInsnNode) insn).owner.equals(classNode.name)) {
                String newName =
                        "m" + toHexString(sha256(initializer.toString())).substring(0, 16);
                newNames.putIfAbsent(((FieldInsnNode) insn).name, newName);
                initializer.setLength(0);
            } else if (insn.getOpcode() >= 0) {
                initializer.append(instruction(insn, labels)).append('\n');
            }
        }

        for (FieldNode field : classNode.fields) {
            field.name = newNames.getOrDefault(field.name, field.name);
        }
        for (MethodNode method : classNode.methods) {
            for (AbstractInsnNode insn : method.instructions) {
                if (insn instanceof FieldInsnNode && ((FieldInsnNode) insn).owner.equals(classNode.name)) {
                    FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                    fieldInsn.name = newNames.getOrDefault(fieldInsn.name, fieldInsn.name);
                }
            }
        }
    }

    /**
     * Renumbers the synthetic {@code lambda$<method>$<N>} methods from zero within each enclosing method, in declaration
     * order, so the hash does not depend on the compiling JDK: javac 23 and older count {@code N} across the class,
     * javac 24 and newer per enclosing method, but both declare the methods in the same order. References to the
     * renamed methods are rewritten too. Nested lambdas, whose names embed the outer lambda's number, are not fully
     * normalized.
     */
    private static void renameLambdaMethods(ClassNode classNode) {
        Map<String, Integer> counts = new HashMap<>();
        Map<String, String> newNames = new HashMap<>();
        for (MethodNode method : classNode.methods) {
            if ((method.access & Opcodes.ACC_SYNTHETIC) != 0 && method.name.startsWith("lambda$")) {
                String prefix = method.name.substring(0, method.name.lastIndexOf('$') + 1);
                newNames.put(method.name, prefix + (counts.merge(prefix, 1, Integer::sum) - 1));
            }
        }
        if (newNames.isEmpty()) {
            return;
        }
        for (MethodNode method : classNode.methods) {
            method.name = newNames.getOrDefault(method.name, method.name);
            for (AbstractInsnNode insn : method.instructions) {
                if (insn instanceof InvokeDynamicInsnNode) {
                    Object[] bsmArgs = ((InvokeDynamicInsnNode) insn).bsmArgs;
                    for (int i = 0; i < bsmArgs.length; i++) {
                        if (bsmArgs[i] instanceof Handle) {
                            Handle handle = (Handle) bsmArgs[i];
                            if (handle.getOwner().equals(classNode.name) && newNames.containsKey(handle.getName())) {
                                bsmArgs[i] = new Handle(
                                        handle.getTag(),
                                        handle.getOwner(),
                                        newNames.get(handle.getName()),
                                        handle.getDesc(),
                                        handle.isInterface());
                            }
                        }
                    }
                } else if (insn instanceof MethodInsnNode) {
                    MethodInsnNode methodInsn = (MethodInsnNode) insn;
                    if (methodInsn.owner.equals(classNode.name) && newNames.containsKey(methodInsn.name)) {
                        methodInsn.name = newNames.get(methodInsn.name);
                    }
                }
            }
        }
    }

    /**
     * Sorts the blocks of a proxy's {@code <clinit>} that each end by assigning a static field of the class. The
     * instructions after the last assignment, like the return and the exception handlers, keep their position.
     */
    private static List<String> sortInitializers(List<String> instructions, String className) {
        String assignment = Printer.OPCODES[Opcodes.PUTSTATIC] + " " + className + ".";
        List<String> blocks = new ArrayList<>();
        StringBuilder block = new StringBuilder();
        for (String instruction : instructions) {
            block.append(instruction).append('\n');
            if (instruction.startsWith(assignment)) {
                blocks.add(block.toString());
                block.setLength(0);
            }
        }
        blocks.sort(null);
        List<String> sorted = new ArrayList<>(blocks);
        sorted.add(block.toString());
        return sorted;
    }

    private static List<String> instructions(MethodNode method) {
        Map<LabelNode, Integer> labels = labelPositions(method);
        List<String> instructions = new ArrayList<>();
        for (AbstractInsnNode insn : method.instructions) {
            if (insn.getOpcode() >= 0) {
                instructions.add(instruction(insn, labels));
            }
        }
        return instructions;
    }

    /**
     * Maps each label to the index of the next instruction, so that jump targets do not depend on how labels are
     * numbered.
     */
    private static Map<LabelNode, Integer> labelPositions(MethodNode method) {
        Map<LabelNode, Integer> labels = new HashMap<>();
        int position = 0;
        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof LabelNode) {
                labels.put((LabelNode) insn, position);
            } else if (insn.getOpcode() >= 0) {
                position++;
            }
        }
        return labels;
    }

    private static String instruction(AbstractInsnNode insn, Map<LabelNode, Integer> labels) {
        String opcode = Printer.OPCODES[insn.getOpcode()];
        switch (insn.getType()) {
            case AbstractInsnNode.INT_INSN:
                return opcode + " " + ((IntInsnNode) insn).operand;
            case AbstractInsnNode.VAR_INSN:
                return opcode + " " + ((VarInsnNode) insn).var;
            case AbstractInsnNode.TYPE_INSN:
                return opcode + " " + ((TypeInsnNode) insn).desc;
            case AbstractInsnNode.FIELD_INSN:
                FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                return opcode + " " + fieldInsn.owner + "." + fieldInsn.name + " " + fieldInsn.desc;
            case AbstractInsnNode.METHOD_INSN:
                MethodInsnNode methodInsn = (MethodInsnNode) insn;
                return opcode + " " + methodInsn.owner + "." + methodInsn.name + methodInsn.desc + " " + methodInsn.itf;
            case AbstractInsnNode.INVOKE_DYNAMIC_INSN:
                InvokeDynamicInsnNode indy = (InvokeDynamicInsnNode) insn;
                return opcode + " " + indy.name + indy.desc + " " + indy.bsm + " " + Arrays.toString(indy.bsmArgs);
            case AbstractInsnNode.JUMP_INSN:
                return opcode + " " + labels.get(((JumpInsnNode) insn).label);
            case AbstractInsnNode.LDC_INSN:
                Object constant = ((LdcInsnNode) insn).cst;
                return opcode + " " + constant.getClass().getSimpleName() + " " + constant(constant);
            case AbstractInsnNode.IINC_INSN:
                IincInsnNode iinc = (IincInsnNode) insn;
                return opcode + " " + iinc.var + " " + iinc.incr;
            case AbstractInsnNode.TABLESWITCH_INSN:
                TableSwitchInsnNode tableSwitch = (TableSwitchInsnNode) insn;
                return opcode + " " + tableSwitch.min + " " + tableSwitch.max + " " + labels.get(tableSwitch.dflt) + " "
                        + positions(tableSwitch.labels, labels);
            case AbstractInsnNode.LOOKUPSWITCH_INSN:
                LookupSwitchInsnNode lookupSwitch = (LookupSwitchInsnNode) insn;
                return opcode + " " + lookupSwitch.keys + " " + labels.get(lookupSwitch.dflt) + " "
                        + positions(lookupSwitch.labels, labels);
            case AbstractInsnNode.MULTIANEWARRAY_INSN:
                MultiANewArrayInsnNode multiANewArray = (MultiANewArrayInsnNode) insn;
                return opcode + " " + multiANewArray.desc + " " + multiANewArray.dims;
            default:
                return opcode;
        }
    }

    /**
     * Renders a constant so that the hash does not depend on the JDK that computes it. {@code Float.toString} and
     * {@code Double.toString} print some values differently since JDK 19 (JDK-4511638), for example
     * {@code (float) Integer.MAX_VALUE} as {@code 2.14748365E9} before and {@code 2.1474836E9} after, so floating-point
     * constants are rendered as their raw bits instead. Other constants render like their {@code toString}.
     */
    private static String constant(Object value) {
        if (value instanceof Float) {
            return Integer.toHexString(Float.floatToRawIntBits((Float) value));
        }
        if (value instanceof Double) {
            return Long.toHexString(Double.doubleToRawLongBits((Double) value));
        }
        return String.valueOf(value);
    }

    private static List<Integer> positions(List<LabelNode> targets, Map<LabelNode, Integer> labels) {
        List<Integer> positions = new ArrayList<>();
        for (LabelNode target : targets) {
            positions.add(labels.get(target));
        }
        return positions;
    }

    private static byte[] sha256(String text) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toHexString(byte[] bytes) {
        Formatter result = new Formatter();
        try (result) {
            for (byte b : bytes) {
                result.format(toHexString(b));
            }
            return result.toString();
        }
    }

    public static String toHexString(byte b) {
        return String.format("%02x", b & 0xff);
    }
}
