package com.asdflj.ae2thing.coremod.transform;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.asdflj.ae2thing.coremod.ClassTransformer;

public class TileThaumatoriumTransformer extends ClassTransformer.ClassMapper {

    public static final TileThaumatoriumTransformer INSTANCE = new TileThaumatoriumTransformer();

    public TileThaumatoriumTransformer() {}

    @Override
    protected ClassVisitor getClassMapper(ClassVisitor downstream) {
        return new TileThaumatoriumTransformer.TransformTileThaumatorium(Opcodes.ASM5, downstream);
    }

    @Override
    public byte[] transformClass(byte[] code) {
        ClassReader reader = new ClassReader(code);
        ClassWriter writer = new ClassWriter(reader, getWriteFlags());
        reader.accept(getClassMapper(writer), ClassReader.EXPAND_FRAMES);
        return writer.toByteArray();
    }

    private static class TransformTileThaumatorium extends ClassVisitor {

        public TransformTileThaumatorium(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (name.equals("fill")) {
                return new TransformTileThaumatorium.TransformFill(
                    api,
                    super.visitMethod(access, name, desc, signature, exceptions));
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

        private static class TransformFill extends MethodVisitor {

            int c = 0;

            TransformFill(int api, MethodVisitor mv) {
                super(api, mv);
            }

            @Override
            public void visitVarInsn(int opcode, int var) {
                super.visitVarInsn(opcode, var);
                if (opcode == Opcodes.ASTORE && var == 1 && c == 1) {
                    visitVarInsn(Opcodes.ALOAD, 0);
                    visitVarInsn(Opcodes.ILOAD, 3);
                    visitVarInsn(Opcodes.ALOAD, 7);
                    super.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "com/asdflj/ae2thing/coremod/hooker/CoreModHooks",
                        "getConnectableTile",
                        "(Lthaumcraft/common/tiles/TileThaumatorium;ILnet/minecraftforge/common/util/ForgeDirection;)V",
                        false);
                    c = 0;
                }
            }

            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                if (name.equals("getConnectableTile")) {
                    c = 1;
                }
            }
        }
    }
}
