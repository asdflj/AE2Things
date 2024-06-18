package com.asdflj.ae2thing.common.storage.backpack;

import com.glodblock.github.common.item.ItemFluidDrop;
import net.minecraft.item.ItemStack;

import com.darkona.adventurebackpack.inventory.InventoryBackpack;

import appeng.util.Platform;

public class AdventureBackpackHandler extends BaseBackpackHandler {

    private final InventoryBackpack inventory;

    public AdventureBackpackHandler(ItemStack is) {
        super(new InventoryBackpack(is));
        this.inventory = (InventoryBackpack) inv;
    }

    @Override
    public ItemStack getStackInSlot(int slotIn) {
        if(slotIn == this.getSizeInventory()-1){
            return ItemFluidDrop.newStack(this.inventory.getRightTank().getFluid());
        } else if (slotIn == this.getSizeInventory() -2) {
            return ItemFluidDrop.newStack(this.inventory.getLeftTank().getFluid());
        }else {
            return super.getStackInSlot(slotIn);
        }
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack is) {
        ItemStack slotItem = inv.getStackInSlot(slot);
        if (slotItem == null) return true;
        if (!Platform.isSameItemPrecise(is, slotItem)) return false;
        return slotItem.stackSize < slotItem.getMaxStackSize();
    }

    @Override
    public int getSizeInventory() {
        return super.getSizeInventory() + 2;
    }
}
