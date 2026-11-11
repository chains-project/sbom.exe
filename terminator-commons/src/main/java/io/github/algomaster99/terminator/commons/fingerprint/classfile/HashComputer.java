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

    // Classes instead of lambdas. The agents run this code while the JDK defines a class. Linking a lambda spins
    // method handles, which can need that class.
    private static final Comparator<FieldNode> FIELD_ORDER = new Comparator<>() {
        @Override
        public int compare(FieldNode a, FieldNode b) {
            int byName = a.name.compareTo(b.name);
            return byName != 0 ? byName : a.desc.compareTo(b.desc);
        }
    };

    private static final Comparator<MethodNode> METHOD_ORDER = new Comparator<>() {
        @Override
        public int compare(MethodNode a, MethodNode b) {
            int byName = a.name.compareTo(b.name);
            return byName != 0 ? byName : a.desc.compareTo(b.desc);
        }
    };

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

        renameLambdaMethods(classNode);
        return toHexString(sha256(canonicalView(classNode)));
    }

    private static String canonicalView(ClassNode classNode) {
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
        fields.sort(FIELD_ORDER);
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
        methods.sort(METHOD_ORDER);
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

            for (String instruction : instructions(method)) {
                sb.append("  ").append(instruction).append('\n');
            }

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
     * Renumbers the synthetic {@code lambda$<method>$<N>} methods from zero within each enclosing method, in declaration
     * order, so the hash does not depend on the compiling JDK: javac 23 and older count {@code N} across the class,
     * javac 24 and newer per enclosing method, but both declare the methods in the same order. References to the
     * renamed methods are rewritten too. Nested lambdas, whose names embed the outer lambda's number, are not fully
     * normalized.
     */
    private static void renameLambdaMethods(ClassNode classNode) {
        Map<String, Integer> counts = new HashMap<>();
        Map<String, String> newNames = new HashMap<>();
        // Assumes javac declares the lambda methods in the same order on every JDK (observed for javac 8 to 25).
        for (MethodNode method : classNode.methods) {
            if ((method.access & Opcodes.ACC_SYNTHETIC) != 0 && method.name.startsWith("lambda$")) {
                String prefix = method.name.substring(0, method.name.lastIndexOf('$') + 1);
                int number = counts.getOrDefault(prefix, 0);
                counts.put(prefix, number + 1);
                newNames.put(method.name, prefix + number);
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
