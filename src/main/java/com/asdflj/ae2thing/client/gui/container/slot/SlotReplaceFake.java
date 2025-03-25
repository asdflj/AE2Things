package com.asdflj.ae2thing.client.gui.container.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.glodblock.github.common.item.ItemFluidPacket;

import appeng.container.slot.SlotFake;
import codechicken.nei.recipe.StackInfo;

public class SlotReplaceFake extends SlotFake {

    public SlotReplaceFake(IInventory inv, int idx, int x, int y) {
        super(inv, idx, x, y);
    }

    @Override
    public void putStack(ItemStack is) {
        if (is != null) {
            is = is.copy();
            is.stackSize = 1;
            FluidStack fs = StackInfo.getFluid(is);
            if (fs != null) {
                is = ItemFluidPacket.newStack(fs);
            }
        }
        super.putStack(is);
    }
}
