package io.github.algomaster99.terminator.commons.fingerprint.classfile;

import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

public class ClassUtils {
    public static List<String> getInnerClasses(byte[] classfileBuffer) {
        List<String> innerClasses = new ArrayList<>();
        ClassReader reader = new ClassReader(classfileBuffer);
        ClassVisitor visitor = new ClassVisitor(Opcodes.ASM7) {
            @Override
            public void visitInnerClass(String name, String outerName, String innerName, int access) {
                innerClasses.add(name);
            }
        };
        reader.accept(visitor, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return innerClasses;
    }

    public static boolean isInnerClass(String className, byte[] classfileBuffer) {
        List<String> innerClasses = getInnerClasses(classfileBuffer);
        for (String innerClass : innerClasses) {
            if (innerClass.equals(className)) {
                return true;
            }
        }
        return false;
    }

    public static String getOutermostClass(byte[] classfileBuffer) {
        ClassReader reader = new ClassReader(classfileBuffer);
        OutermostClassVisitor visitor = new OutermostClassVisitor();
        reader.accept(visitor, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return visitor.getOutermostClass();
    }

    private static class OutermostClassVisitor extends ClassVisitor {
        private String outermostClass;

        public OutermostClassVisitor() {
            super(Opcodes.ASM7);
        }

        @Override
        public void visit(
                int version, int access, String name, String signature, String superName, String[] interfaces) {
            outermostClass = name;
        }

        public String getOutermostClass() {
            return outermostClass;
        }
    }
}
