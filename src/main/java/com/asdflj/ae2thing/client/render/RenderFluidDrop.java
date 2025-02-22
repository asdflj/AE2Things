package com.asdflj.ae2thing.client.render;

import static appeng.client.gui.AEBaseGui.aeRenderItem;

import java.util.function.Predicate;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import com.asdflj.ae2thing.client.gui.IGuiDrawSlot;
import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.crossmod.thaumcraft.AspectRender;
import com.glodblock.github.crossmod.thaumcraft.AspectUtil;

import appeng.api.storage.data.IAEItemStack;

public class RenderFluidDrop implements ISlotRender {

    @Override
    public Predicate<Slot> get() {
        return slot -> slot.getStack()
            .getItem() instanceof ItemFluidDrop;
    }

    @Override
    public boolean drawSlot(Slot slot, IAEItemStack stack, IGuiDrawSlot draw, boolean display) {
        FluidStack fluidStack = ItemFluidDrop.getFluidStack(slot.getStack());
        if (fluidStack == null || fluidStack.getFluid() == null) return true;
        if (ModAndClassUtil.THE && AspectUtil.isEssentiaGas(fluidStack)) {
            GL11.glTranslatef(0.0f, 0.0f, 150.0f);
            AspectRender.drawAspect(
                Minecraft.getMinecraft().thePlayer,
                slot.xDisplayPosition,
                slot.yDisplayPosition,
                draw.getzLevel(),
                AspectUtil.getAspectFromGas(fluidStack),
                fluidStack.amount <= 0 ? 1 : fluidStack.amount);
            GL11.glTranslatef(0.0f, 0.0f, -150.0f);
            IAEItemStack gas = stack.copy()
                .setStackSize(stack.getStackSize() / AspectUtil.R);
            aeRenderItem.setAeStack(gas);
            draw.renderStackSize(display, stack, slot);
        } else {
            draw.drawWidget(slot.xDisplayPosition, slot.yDisplayPosition, fluidStack.getFluid());
            aeRenderItem.setAeStack(stack);
            draw.renderStackSize(display, stack, slot);
        }
        return false;
    }
}
