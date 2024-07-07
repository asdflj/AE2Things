package com.asdflj.ae2thing.coremod.transform;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.asdflj.ae2thing.coremod.ClassTransformer;

public class PlatformTransformer extends ClassTransformer.ClassMapper {

    public static final PlatformTransformer INSTANCE = new PlatformTransformer();

    public PlatformTransformer() {}

    @Override
    protected ClassVisitor getClassMapper(ClassVisitor downstream) {
        return new PlatformTransformer.TransformPlatform(Opcodes.ASM5, downstream);
    }

    private static class TransformPlatform extends ClassVisitor {

        public TransformPlatform(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (name.equals("updateView")) {
                return new TransformUpdateView(api, super.visitMethod(access, name, desc, signature, exceptions));
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

        private static class TransformUpdateView extends MethodVisitor {

            TransformUpdateView(int api, MethodVisitor mv) {
                super(api, mv);
            }

            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                switch (name) {
                    case "getModId" -> super.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "com/asdflj/ae2thing/coremod/hooker/CoreModHooksClient",
                        "getModId",
                        "(Lappeng/api/storage/data/IAEItemStack;)Ljava/lang/String;",
                        false);
                    case "getItemDisplayName" -> super.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "com/asdflj/ae2thing/coremod/hooker/CoreModHooksClient",
                        "getItemDisplayName",
                        "(Ljava/lang/Object;)Ljava/lang/String;",
                        false);
                    case "getTooltip" -> super.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "com/asdflj/ae2thing/coremod/hooker/CoreModHooksClient",
                        "getTooltip",
                        "(Ljava/lang/Object;)Ljava/util/List;",
                        false);
                    default -> super.visitMethodInsn(opcode, owner, name, desc, itf);
                }

            }
        }
    }

}
