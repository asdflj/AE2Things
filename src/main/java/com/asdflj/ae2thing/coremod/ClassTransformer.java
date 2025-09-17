package com.asdflj.ae2thing.coremod;

import net.minecraft.launchwrapper.IClassTransformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import com.asdflj.ae2thing.coremod.transform.CraftingJobV2Transformer;
import com.asdflj.ae2thing.coremod.transform.FluidConvertingInventoryAdaptorTransformer;
import com.asdflj.ae2thing.coremod.transform.GuiDualInterfaceTransformer;
import com.asdflj.ae2thing.coremod.transform.PlatformTransformer;
import com.asdflj.ae2thing.coremod.transform.TileThaumatoriumTransformer;

public class ClassTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] code) {
        Transform tform;
        switch (transformedName) {
            case "appeng.client.me.ItemRepo" -> tform = PlatformTransformer.INSTANCE;
            case "com.glodblock.github.inventory.FluidConvertingInventoryAdaptor" -> tform = FluidConvertingInventoryAdaptorTransformer.INSTANCE;
            case "thaumcraft.common.tiles.TileThaumatorium" -> tform = TileThaumatoriumTransformer.INSTANCE;
            case "com.glodblock.github.client.gui.GuiDualInterface" -> tform = GuiDualInterfaceTransformer.INSTANCE;
            case "appeng.crafting.v2.CraftingJobV2", "appeng.me.GridStorage" -> tform = CraftingJobV2Transformer.INSTANCE;
            default -> {
                return code;
            }
        }
        System.out.println("[AE2TH] Transforming class: " + transformedName);
        return tform.transformClass(code);
    }

    public interface Transform {

        byte[] transformClass(byte[] code);
    }

    public abstract static class ClassMapper implements Transform {

        @Override
        public byte[] transformClass(byte[] code) {
            ClassReader reader = new ClassReader(code);
            ClassWriter writer = new ClassWriter(reader, getWriteFlags());
            reader.accept(getClassMapper(writer), ClassReader.EXPAND_FRAMES);
            return writer.toByteArray();
        }

        protected int getWriteFlags() {
            return 0;
        }

        protected abstract ClassVisitor getClassMapper(ClassVisitor downstream);
    }
}
