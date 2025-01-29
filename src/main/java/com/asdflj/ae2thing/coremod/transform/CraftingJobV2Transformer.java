package com.asdflj.ae2thing.coremod.transform;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.asdflj.ae2thing.coremod.ClassTransformer;

public class CraftingJobV2Transformer extends ClassTransformer.ClassMapper {

    public static CraftingJobV2Transformer INSTANCE = new CraftingJobV2Transformer();

    public CraftingJobV2Transformer() {}

    @Override
    protected ClassVisitor getClassMapper(ClassVisitor downstream) {
        return new TransformCraftingJobV2(Opcodes.ASM5, downstream);
    }

    private static class TransformCraftingJobV2 extends ClassVisitor {

        public TransformCraftingJobV2(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (name.equals("<init>") && desc.equals(
                "(Lnet/minecraft/world/World;Lappeng/api/networking/IGrid;Lappeng/api/networking/security/BaseActionSource;Lappeng/api/storage/data/IAEItemStack;Lappeng/api/config/CraftingMode;Lappeng/api/networking/crafting/ICraftingCallback;)V")) {
                return new TransformInit(api, super.visitMethod(access, name, desc, signature, exceptions));
            } else if (name.equals("remove")) {
                return new TransformRemove(api, super.visitMethod(access, name, desc, signature, exceptions));
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

        private static class TransformRemove extends MethodVisitor {

            public TransformRemove(int api, MethodVisitor mv) {
                super(api, mv);
            }

            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                if (opcode == Opcodes.INVOKESTATIC) {
                    super.visitVarInsn(Opcodes.ALOAD, 0);
                    super.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "com/asdflj/ae2thing/api/CraftingDebugHelper",
                        "remove",
                        "(Lappeng/me/GridStorage;)V",
                        false);
                }
            }
        }

        private static class TransformInit extends MethodVisitor {

            TransformInit(int api, MethodVisitor mv) {
                super(api, mv);
            }

            @Override
            public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                super.visitFieldInsn(opcode, owner, name, desc);
                if (name.equals("callback")) {
                    super.visitVarInsn(Opcodes.ALOAD, 0);
                    super.visitVarInsn(Opcodes.ALOAD, 1);
                    super.visitVarInsn(Opcodes.ALOAD, 2);
                    super.visitVarInsn(Opcodes.ALOAD, 3);
                    super.visitVarInsn(Opcodes.ALOAD, 4);
                    super.visitVarInsn(Opcodes.ALOAD, 5);
                    super.visitVarInsn(Opcodes.ALOAD, 6);
                    super.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "com/asdflj/ae2thing/api/CraftingDebugHelper",
                        "craftingHelper",
                        "(Lappeng/crafting/v2/CraftingJobV2;Lnet/minecraft/world/World;Lappeng/api/networking/IGrid;Lappeng/api/networking/security/BaseActionSource;Lappeng/api/storage/data/IAEItemStack;Lappeng/api/config/CraftingMode;Lappeng/api/networking/crafting/ICraftingCallback;)V",
                        false);
                }
            }
        }
    }
}
