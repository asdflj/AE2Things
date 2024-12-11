package com.asdflj.ae2thing.common.storage.backpack;

import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import appeng.util.Platform;
import forestry.storage.inventory.ItemInventoryBackpack;
import forestry.storage.items.ItemBackpack;

public class FTRBackpackHandler extends BaseBackpackHandler {

    public FTRBackpackHandler(EntityPlayer player, ItemStack is) {
        super(
            new ItemInventoryBackpack(
                player,
                ((ItemBackpack) Objects.requireNonNull(is.getItem())).getBackpackSize(),
                is));
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack is) {
        if (!this.inv.isItemValidForSlot(slot, is)) return false;
        ItemStack slotItem = inv.getStackInSlot(slot);
        if (slotItem == null) return true;
        if (!Platform.isSameItemPrecise(is, slotItem)) return false;
        return slotItem.stackSize < slotItem.getMaxStackSize();
    }
}
