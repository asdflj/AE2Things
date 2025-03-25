package com.asdflj.ae2thing.client.gui.container.slot;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import appeng.container.slot.OptionalSlotRestrictedInput;

public class SlotEncodedPatternInput extends OptionalSlotRestrictedInput {

    public SlotEncodedPatternInput(IInventory i, int slotIndex, int x, int y, InventoryPlayer invPlayer) {
        super(PlacableItemType.ENCODED_PATTERN, i, idx -> true, slotIndex, x, y, 0, invPlayer);
        setStackLimit(1);
    }

    @Override
    public void putStack(ItemStack par1ItemStack) {
        super.putStack(par1ItemStack);
        this.onSlotChanged();
    }

    @Override
    public void onSlotChange(ItemStack p_75220_1_, ItemStack p_75220_2_) {
        super.onSlotChange(p_75220_1_, p_75220_2_);
    }
}
