package com.asdflj.ae2thing.common.storage.backpack;

import net.minecraft.item.ItemStack;

import com.darkona.adventurebackpack.inventory.InventoryBackpack;

import appeng.util.Platform;

public class AdventureBackpackHandler extends BaseBackpackHandler {

    public AdventureBackpackHandler(ItemStack is) {
        super(new InventoryBackpack(is));
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack is) {
        ItemStack slotItem = inv.getStackInSlot(slot);
        if (slotItem == null) return true;
        if (!Platform.isSameItemPrecise(is, slotItem)) return false;
        return slotItem.stackSize < slotItem.getMaxStackSize();
    }
}
