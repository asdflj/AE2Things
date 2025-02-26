package com.asdflj.ae2thing.coremod.transform;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.asdflj.ae2thing.coremod.ClassTransformer;

public class BRTransformer extends ClassTransformer.ClassMapper {

    public static BRTransformer INSTANCE = new BRTransformer();

    public BRTransformer() {}

    @Override
    protected ClassVisitor getClassMapper(ClassVisitor downstream) {
        return new BRTransformer.TransformBR(Opcodes.ASM5, downstream);
    }

    private static class TransformBR extends ClassVisitor {

        public TransformBR(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (name.equals("neiOverlay") && desc.equals("(Lblockrenderer6343/client/renderer/WorldSceneRenderer;)V")) {
                return new TransformneiOverlay(api, super.visitMethod(access, name, desc, signature, exceptions), 0);
            } else if (name.equals("neiOverlay") && desc.equals("()V")) {
                return new TransformneiOverlay(api, super.visitMethod(access, name, desc, signature, exceptions), 1);
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

        private static class TransformneiOverlay extends MethodVisitor {

            private final int type;

            public TransformneiOverlay(int api, MethodVisitor mv, int type) {
                super(api, mv);
                this.type = type;
            }

            @Override
            public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                if (name.equals("isNEELoaded")) {
                    super.visitFieldInsn(
                        Opcodes.GETSTATIC,
                        "com/asdflj/ae2thing/coremod/hooker/CoreModBRHookClient",
                        "isNEELoaded",
                        "Z");
                } else {
                    super.visitFieldInsn(opcode, owner, name, desc);
                }
            }

            @Override
            public void visitInsn(int opcode) {
                if (opcode == Opcodes.RETURN) {
                    if (type == 0) {
                        mv.visitVarInsn(Opcodes.ALOAD, 0);
                        super.visitMethodInsn(
                            Opcodes.INVOKESTATIC,
                            "com/asdflj/ae2thing/coremod/hooker/CoreModBRHookClient",
                            "neiOverlay",
                            "(Lblockrenderer6343/client/renderer/WorldSceneRenderer;)V",
                            false);
                    } else if (type == 1) {
                        mv.visitVarInsn(Opcodes.ALOAD, 0);
                        super.visitMethodInsn(
                            Opcodes.INVOKESTATIC,
                            "com/asdflj/ae2thing/coremod/hooker/CoreModBRHookClient",
                            "neiOverlay",
                            "(Ljava/lang/Object;)V",
                            false);
                    }

                }
                super.visitInsn(opcode);
            }
        }
    }
}
