package com.asdflj.ae2thing.coremod.transform;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.asdflj.ae2thing.coremod.ClassTransformer;

public class TileIOPortTransformer extends ClassTransformer.ClassMapper {

    public static final TileIOPortTransformer INSTANCE = new TileIOPortTransformer();

    public TileIOPortTransformer() {}

    @Override
    protected ClassVisitor getClassMapper(ClassVisitor downstream) {
        return new TileIOPortTransformer.TransformTileIOPort(Opcodes.ASM5, downstream);
    }

    private static class TransformTileIOPort extends ClassVisitor {

        public TransformTileIOPort(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (name.equals("tickingRequest")) {
                return new TransformTickingRequest(api, super.visitMethod(access, name, desc, signature, exceptions));
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

        private static class TransformTickingRequest extends MethodVisitor {

            private static int n = 0;

            TransformTickingRequest(int api, MethodVisitor mv) {
                super(api, mv);
            }

            @Override
            public void visitVarInsn(int opcode, int var) {
                super.visitVarInsn(opcode, var);
                if (opcode == Opcodes.LSTORE && n == 0) {
                    n++;
                    super.visitVarInsn(Opcodes.ALOAD, 0);
                    super.visitVarInsn(Opcodes.LLOAD, 3);
                    super.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "com/asdflj/ae2thing/coremod/hooker/CoreModHooks",
                        "getItemsToMove",
                        "(Lappeng/tile/storage/TileIOPort;J)J",
                        false);
                    super.visitVarInsn(Opcodes.LSTORE, 3);
                }

            }
        }
    }
}
