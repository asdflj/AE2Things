package com.asdflj.ae2thing.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import appeng.tile.inventory.BiggerAppEngInventory;
import appeng.util.Platform;

public class ItemBiggerAppEngInventory extends BiggerAppEngInventory {

    private final ItemStack is;
    private final String name;
    private final EntityPlayer player;
    private final int slot;

    public ItemBiggerAppEngInventory(ItemStack is, String name, int size, EntityPlayer player, int slot) {
        super(null, size);
        this.name = name;
        this.is = is;
        this.player = player;
        this.slot = slot;
        this.readFromNBT(Platform.openNbtData(is), name);
    }

    @Override
    public void markDirty() {
        this.writeToNBT(Platform.openNbtData(is), this.name);
        if (Platform.isServer()) this.player.inventory.setInventorySlotContents(slot, this.is);
    }
}
