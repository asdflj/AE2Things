package com.asdflj.ae2thing.client.gui.container.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.glodblock.github.inventory.AEFluidInventory;

import appeng.container.slot.IOptionalSlotHost;
import appeng.container.slot.OptionalSlotFakeTypeOnly;

public class ManaSlotFake extends OptionalSlotFakeTypeOnly {

    AEFluidInventory fluidInv;

    public ManaSlotFake(IInventory inv, AEFluidInventory fluidInv, IOptionalSlotHost containerBus, int idx, int x,
        int y, int offX, int offY, int groupNum) {
        super(inv, containerBus, idx, x, y, offX, offY, groupNum);
        this.fluidInv = fluidInv;
    }

    @Override
    public void putStack(ItemStack is) {

    }
}
