package com.asdflj.ae2thing.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.core.AELog;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;

public class ItemBiggerAppEngInventory extends AppEngInternalInventory {

    private final ItemStack is;
    private final String name;
    private final EntityPlayer player;
    private final int slot;
    private final IAEAppEngInventory terminal;
    private static final int MAX_SIZE = 64;

    public ItemBiggerAppEngInventory(ItemStack is, String name, int size, EntityPlayer player, int slot) {
        this(is, name, size, player, slot, null, MAX_SIZE);
    }

    public ItemBiggerAppEngInventory(ItemStack is, String name, int size, EntityPlayer player, int slot,
        IAEAppEngInventory terminal, int maxsize) {
        super(null, size, maxsize);
        this.name = name;
        this.is = is;
        this.player = player;
        this.slot = slot;
        this.terminal = terminal;
        this.readFromNBT(Platform.openNbtData(is), name);
        this.setMaxStackSize(maxsize);
    }

    public ItemBiggerAppEngInventory(ItemStack is, String name, int size, EntityPlayer player, int slot,
        IAEAppEngInventory terminal) {
        this(is, name, size, player, slot, terminal, MAX_SIZE);
    }

    protected void writeToNBT(final NBTTagCompound target) {
        for (int x = 0; x < this.getSizeInventory(); x++) {
            try {
                final NBTTagCompound c = new NBTTagCompound();

                if (this.inv[x] != null) {
                    Platform.writeItemStackToNBT(this.inv[x], c);
                }

                target.setTag("#" + x, c);
            } catch (final Exception ignored) {}
        }
    }

    public void readFromNBT(final NBTTagCompound target) {
        for (int x = 0; x < this.getSizeInventory(); x++) {
            try {
                final NBTTagCompound c = target.getCompoundTag("#" + x);

                if (c != null) {
                    this.inv[x] = Platform.loadItemStackFromNBT(c);
                }
            } catch (final Exception e) {
                AELog.debug(e);
            }
        }
    }

    @Override
    public void markDirty() {
        this.writeToNBT(Platform.openNbtData(is), this.name);
        if (Platform.isServer()) {
            if (slot != -1) {
                this.player.inventory.setInventorySlotContents(slot, this.is);
            } else {
                this.player.inventory.setItemStack(this.is);
            }
        }
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
