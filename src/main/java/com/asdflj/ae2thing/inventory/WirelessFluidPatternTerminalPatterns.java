package com.asdflj.ae2thing.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.api.Constants;

import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;

public class WirelessFluidPatternTerminalPatterns extends AppEngInternalInventory {

    private final ItemStack is;
    private final IAEAppEngInventory terminal;
    private final EntityPlayer player;
    private final int slot;

    public WirelessFluidPatternTerminalPatterns(final ItemStack is, final IAEAppEngInventory term, EntityPlayer player,
        int slot) {
        super(null, 2);
        this.is = is;
        this.terminal = term;
        this.player = player;
        this.slot = slot;
        this.readFromNBT(Platform.openNbtData(is), Constants.PATTERN);
    }

    @Override
    public void markDirty() {
        this.writeToNBT(Platform.openNbtData(is), Constants.PATTERN);
        if (Platform.isServer()) this.player.inventory.setInventorySlotContents(slot, this.is);
    }

    @Override
    public void markDirty(int slotIndex) {
        this.markDirty();
    }

    @Override
    public void setInventorySlotContents(final int slot, final ItemStack newItemStack) {
        final ItemStack oldStack = this.inv[slot];
        this.inv[slot] = newItemStack;

        if (this.eventsEnabled()) {
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
