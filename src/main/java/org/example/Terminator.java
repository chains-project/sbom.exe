package org.example;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.constant.IntegerConstant;
import net.bytebuddy.implementation.bytecode.constant.TextConstant;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.TraceClassVisitor;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

public class Terminator {
    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(
                new ClassFileTransformer() {
                    @Override
                    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
                        try {
                            return terminationCode(className, classfileBuffer);
                        } catch (NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        );
    }

    private static byte[] terminationCode(String className, byte[] classfileBuffer) throws NoSuchMethodException {
        String prohibitedClassName = "org/apache/commons/math3/analysis/function/Subtract";
        if (className.equals("foo/Main")) {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(classfileBuffer);
            classReader.accept(classNode, 0);
            for (MethodNode method : classNode.methods) {
                for (AbstractInsnNode insnNode: method.instructions) {
                    // at each method call, check if the owner is the prohibited class
                    if (insnNode instanceof MethodInsnNode) {
                        if (((MethodInsnNode) insnNode).owner.equals(prohibitedClassName)) {
                            AbstractInsnNode currNode = insnNode;

                            List<StackManipulation> manipulations = new ArrayList<>();

                            // add the code to print and exit
                            manipulations.add(new TextConstant("You accidentally have included `" + prohibitedClassName + "` in the app"));
                            manipulations.add(MethodInvocation.invoke(new MethodDescription.ForLoadedMethod(Terminator.class.getMethod("printer", String.class))));
                            manipulations.add(IntegerConstant.forValue(1));
                            manipulations.add(MethodInvocation.invoke(new MethodDescription.ForLoadedMethod(Terminator.class.getMethod("exit", int.class))));

                            StackManipulation operations = new StackManipulation.Compound(manipulations);

                            currNode = ByteBuddyHelper.applyStackManipulation(method, currNode, operations, ByteBuddyHelper.InsertPosition.BEFORE);
                        }
                    }
                }
            }
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            classNode.accept(new TraceClassVisitor(writer, null));
            return writer.toByteArray();
        }
        return classfileBuffer;
    }

    public static void exit(int status) {
        System.exit(status);
    }

    public static void printer(String s) {
        System.out.println(s);
    }
}
