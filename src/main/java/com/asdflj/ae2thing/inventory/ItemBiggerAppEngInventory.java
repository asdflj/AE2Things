package com.asdflj.ae2thing.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import appeng.tile.inventory.BiggerAppEngInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;

public class ItemBiggerAppEngInventory extends BiggerAppEngInventory {

    private final ItemStack is;
    private final String name;
    private final EntityPlayer player;
    private final int slot;
    private final IAEAppEngInventory terminal;

    public ItemBiggerAppEngInventory(ItemStack is, String name, int size, EntityPlayer player, int slot) {
        this(is, name, size, player, slot, null);
    }

    public ItemBiggerAppEngInventory(ItemStack is, String name, int size, EntityPlayer player, int slot,
        IAEAppEngInventory terminal) {
        super(null, size);
        this.name = name;
        this.is = is;
        this.player = player;
        this.slot = slot;
        this.terminal = terminal;
        this.readFromNBT(Platform.openNbtData(is), name);
    }

    @Override
    public void markDirty() {
        this.writeToNBT(Platform.openNbtData(is), this.name);
        if (Platform.isServer()) this.player.inventory.setInventorySlotContents(slot, this.is);
    }

    @Override
    public void markDirty(int slotIndex) {
        this.markDirty();
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack newItemStack) {
        final ItemStack oldStack = this.inv[slot];
        this.inv[slot] = newItemStack;

        if (this.eventsEnabled() && this.terminal != null) {
            ItemStack removed = oldStack;
            ItemStack added = newItemStack;

            if (oldStack != null && newItemStack != null && Platform.isSameItemPrecise(oldStack, newItemStack)) {
                if (oldStack.stackSize > newItemStack.stackSize) {
                    removed = removed.copy();
                    removed.stackSize -= newItemStack.stackSize;
                    added = null;
                } else if (oldStack.stackSize < newItemStack.stackSize) {
                    added = added.copy();
                    added.stackSize -= oldStack.stackSize;
                    removed = null;
                } else {
                    removed = added = null;
                }
            }

            this.terminal.onChangeInventory(this, slot, InvOperation.setInventorySlotContents, removed, added);
        }

        this.markDirty();
    }
}
