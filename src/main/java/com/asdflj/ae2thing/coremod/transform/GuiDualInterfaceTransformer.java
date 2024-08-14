package com.asdflj.ae2thing.coremod.transform;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.asdflj.ae2thing.coremod.ClassTransformer;

public class GuiDualInterfaceTransformer extends ClassTransformer.ClassMapper {

    public static final GuiDualInterfaceTransformer INSTANCE = new GuiDualInterfaceTransformer();

    public GuiDualInterfaceTransformer() {}

    @Override
    protected ClassVisitor getClassMapper(ClassVisitor downstream) {
        return new GuiDualInterfaceTransformer.TransformGuiDualInterface(Opcodes.ASM5, downstream);
    }

    private static class TransformGuiDualInterface extends ClassVisitor {

        public TransformGuiDualInterface(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (name.equals("drawFG")) {
                return new TransformDrawFG(api, super.visitMethod(access, name, desc, signature, exceptions));
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

        private static class TransformDrawFG extends MethodVisitor {

            TransformDrawFG(int api, MethodVisitor mv) {
                super(api, mv);
            }

            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                if (name.equals("translateToLocal")) {
                    super.visitVarInsn(Opcodes.ALOAD, 0);
                    super.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "com/asdflj/ae2thing/coremod/hooker/CoreModHooksClient",
                        "translateToLocal",
                        "(Ljava/lang/String;Lcom/glodblock/github/client/gui/GuiDualInterface;)Ljava/lang/String;",
                        false);
                }
            }
        }
    }
}
