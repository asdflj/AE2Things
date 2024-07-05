package com.asdflj.ae2thing.coremod.transform;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.asdflj.ae2thing.coremod.ClassTransformer;

public class CraftingCpuTransformer extends ClassTransformer.ClassMapper {

    public static final CraftingCpuTransformer INSTANCE = new CraftingCpuTransformer();

    private CraftingCpuTransformer() {
        // NO-OP
    }

    @Override
    protected ClassVisitor getClassMapper(ClassVisitor downstream) {
        return new TransformCraftingCPUCluster(Opcodes.ASM5, downstream);
    }

    private static class TransformCraftingCPUCluster extends ClassVisitor {

        TransformCraftingCPUCluster(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (name.equals("completeJob")) {
                return new TransformCompleteJob(api, super.visitMethod(access, name, desc, signature, exceptions));
            } else if (name.equals("submitJob")) {
                return new TransformSubmitJob(api, super.visitMethod(access, name, desc, signature, exceptions));
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

        @Override
        public void visitEnd() {
            FieldVisitor p = cv
                .visitField(Opcodes.ACC_PUBLIC, "player", "Lnet/minecraft/entity/player/EntityPlayer;", null, null);
            p.visitEnd();
            FieldVisitor is = cv
                .visitField(Opcodes.ACC_PUBLIC, "requestItem", "Lappeng/api/storage/data/IAEItemStack;", null, null);
            is.visitEnd();
            super.visitEnd();
        }
    }

    private static class TransformSubmitJob extends MethodVisitor {

        TransformSubmitJob(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode == Opcodes.ARETURN) {
                super.visitVarInsn(Opcodes.ALOAD, 0);
                super.visitVarInsn(Opcodes.ALOAD, 3);
                super.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "com/asdflj/ae2thing/coremod/hooker/CoreModHooks",
                    "craftingAddActionSource",
                    "(Lappeng/me/cluster/implementations/CraftingCPUCluster;Lappeng/api/networking/security/BaseActionSource;)V");
            }
            super.visitInsn(opcode);
        }
    }

    private static class TransformCompleteJob extends MethodVisitor {

        TransformCompleteJob(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode == Opcodes.RETURN) {
                super.visitVarInsn(Opcodes.ALOAD, 0);
                super.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "com/asdflj/ae2thing/coremod/hooker/CoreModHooks",
                    "craftingComplete",
                    "(Lappeng/me/cluster/implementations/CraftingCPUCluster;)V");
            }
            super.visitInsn(opcode);
        }
    }

}
