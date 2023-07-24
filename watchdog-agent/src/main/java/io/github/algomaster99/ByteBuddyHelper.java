package io.github.algomaster99;

import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.dynamic.TargetType;
import net.bytebuddy.dynamic.scaffold.TypeInitializer;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.auxiliary.AuxiliaryType;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

public class ByteBuddyHelper {

    private static final Implementation.Context.ExtractableView BYTE_BUDDY_CONTEXT = Implementation.Context.Disabled.Factory.INSTANCE.make(
            TargetType.DESCRIPTION,
            new AuxiliaryType.NamingStrategy.Enumerating("F"),
            TypeInitializer.None.INSTANCE,
            ClassFileVersion.JAVA_V17,
            ClassFileVersion.JAVA_V17,
            Implementation.Context.FrameGeneration.DISABLED);

    /**
     * Applies a stack manipulation and adds the resulting instructions.
     *
     * @param method the method to add it to
     * @param insertAfter the node to insert it after
     * @param manipulation the stack manipulation to apply
     * @param insertPosition the insertion position
     * @return the new current instruction
     */
    public static AbstractInsnNode applyStackManipulation(
            MethodNode method,
            AbstractInsnNode insertAfter,
            StackManipulation manipulation,
            InsertPosition insertPosition) {
        int sizeBefore = method.instructions.size();

        manipulation.apply(method, BYTE_BUDDY_CONTEXT);

        List<AbstractInsnNode> newNodes = new ArrayList<>();
        for (int i = sizeBefore; i < method.instructions.size(); i++) {
            AbstractInsnNode e = method.instructions.get(i);
            newNodes.add(e);
        }
        newNodes.forEach(it -> method.instructions.remove(it));

        AbstractInsnNode current = insertAfter;
        for (int i = 0; i < newNodes.size(); i++) {
            AbstractInsnNode newNode = newNodes.get(i);
            if (i == 0 && insertPosition == InsertPosition.BEFORE) {
                method.instructions.insertBefore(current, newNode);
            } else {
                method.instructions.insert(current, newNode);
            }
            current = newNode;
        }

        return insertPosition == InsertPosition.BEFORE ? insertAfter : current;
    }

    public enum InsertPosition {
        BEFORE,
        AFTER,
    }
}
