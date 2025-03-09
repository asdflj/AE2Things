package com.asdflj.ae2thing.common.storage.backpack;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidTank;

import com.darkona.adventurebackpack.inventory.InventoryBackpack;

import appeng.util.Platform;

public class AdventureBackpackHandler extends BaseBackpackHandler {

    private final InventoryBackpack inventory;

    public AdventureBackpackHandler(ItemStack is) {
        super(new InventoryBackpack(is));
        this.inventory = (InventoryBackpack) inv;
    }

    @Override
    public boolean hasFluidTank() {
        return true;
    }

    @Override
    public List<FluidTank> getFluidTanks() {
        return Arrays.asList(this.inventory.getLeftTank(), this.inventory.getRightTank());
    }

    @Override
    public int getSizeInventory() {
        return 48;
    }

    @Override
    public void markFluidAsDirty() {
        this.inventory.dirtyTanks();
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack is) {
        ItemStack slotItem = inv.getStackInSlot(slot);
        if (slotItem == null) return true;
        if (!Platform.isSameItemPrecise(is, slotItem)) return false;
        return slotItem.stackSize < slotItem.getMaxStackSize();
    }
}
