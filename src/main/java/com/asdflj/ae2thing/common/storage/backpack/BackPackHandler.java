package com.asdflj.ae2thing.common.storage.backpack;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import appeng.util.Platform;
import de.eydamos.backpack.item.ItemBackpackBase;
import de.eydamos.backpack.saves.BackpackSave;

public class BackPackHandler extends BaseBackpackHandler {

    private final BackpackSave bs;

    public BackPackHandler(EntityPlayer player, ItemStack is) {
        super(ItemBackpackBase.getInventory(is, player));
        bs = new BackpackSave(is);
        ((de.eydamos.backpack.inventory.InventoryBackpack) inv).readFromNBT(bs);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        super.setInventorySlotContents(index, stack);
        this.save();
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack is) {
        ItemStack slotItem = inv.getStackInSlot(slot);
        if (slotItem == null) return true;
        if (!Platform.isSameItemPrecise(is, slotItem)) return false;
        return slotItem.stackSize < slotItem.getMaxStackSize();
    }

    private void save() {
        ((de.eydamos.backpack.inventory.InventoryBackpack) inv).writeToNBT(bs);
    }
}
