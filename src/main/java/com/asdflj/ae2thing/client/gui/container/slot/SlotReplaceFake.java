package com.asdflj.ae2thing.client.gui.container.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import appeng.container.slot.SlotFake;

public class SlotReplaceFake extends SlotFake {

    public SlotReplaceFake(IInventory inv, int idx, int x, int y) {
        super(inv, idx, x, y);
    }

    @Override
    public void putStack(ItemStack is) {
        if (is != null) {
            is = is.copy();
            is.stackSize = 1;
        }
        super.putStack(is);
    }
}
