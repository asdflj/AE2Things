package com.asdflj.ae2thing.coremod.transform;

import com.asdflj.ae2thing.coremod.ClassTransformer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class FluidConvertingInventoryAdaptorTransformer extends ClassTransformer.ClassMapper{
    public static final FluidConvertingInventoryAdaptorTransformer INSTANCE = new FluidConvertingInventoryAdaptorTransformer();

    public FluidConvertingInventoryAdaptorTransformer() {}
    @Override
    protected ClassVisitor getClassMapper(ClassVisitor downstream) {
        return new TransformFluidConvertingInventoryAdaptor(Opcodes.ASM5, downstream);
    }
    private static class TransformFluidConvertingInventoryAdaptor extends ClassVisitor {

        public TransformFluidConvertingInventoryAdaptor(int api, ClassVisitor cv) {
            super(api, cv);
        }
        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (name.equals("wrap")) {
                return new TransformGetAdaptor(api, super.visitMethod(access, name, desc, signature, exceptions));
            } else if (name.equals("getSideItem")) {
                return new TransformGetAdaptor(api, super.visitMethod(access, name, desc, signature, exceptions));
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }


        private static class TransformGetAdaptor extends MethodVisitor {

            TransformGetAdaptor(int api, MethodVisitor mv) {
                super(api, mv);
            }

            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                if(name.equals("getAdaptor")) {
                    super.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "com/asdflj/ae2thing/coremod/hooker/CoreModHooks",
                        "getAdaptor",
                        "(Lnet/minecraft/tileentity/TileEntity;Lnet/minecraftforge/common/util/ForgeDirection;)Lappeng/util/InventoryAdaptor;",
                        false);
                }else{
                    super.visitMethodInsn(opcode, owner, name, desc, itf);
                }


            }
        }
    }
}
