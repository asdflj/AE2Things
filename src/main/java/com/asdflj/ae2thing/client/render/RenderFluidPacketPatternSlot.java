package com.asdflj.ae2thing.client.render;

import static appeng.client.gui.AEBaseGui.aeRenderItem;

import java.util.function.Predicate;

import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.asdflj.ae2thing.client.gui.IGuiDrawSlot;
import com.glodblock.github.common.item.ItemFluidPacket;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.me.SlotME;

public class RenderFluidPacketPatternSlot implements ISlotRender {

    @Override
    public Predicate<Slot> get() {
        return slot -> {
            if (!(slot instanceof SlotME)) {
                ItemStack stack = slot.getStack();
                return stack.getItem() instanceof ItemFluidPacket;
            }
            return false;
        };
    }

    @Override
    public boolean drawSlot(Slot slot, IAEItemStack stack, IGuiDrawSlot draw, boolean display) {
        if (stack.getItem() instanceof ItemFluidPacket && !(slot instanceof SlotME)) {
            FluidStack fluidStack = ItemFluidPacket.getFluidStack(stack);
            if (fluidStack == null || fluidStack.amount <= 0) {
                return true;
            }
            draw.getAEBaseGui()
                .drawMCSlot(slot);
            IAEItemStack fake = stack.copy();
            fake.setStackSize(fluidStack.amount);
            aeRenderItem.setAeStack(fake);
            draw.renderStackSize(display, stack, slot);
            return false;
        }
        return true;
    }
}
