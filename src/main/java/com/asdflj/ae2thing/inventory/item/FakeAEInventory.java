package com.asdflj.ae2thing.inventory.item;

import java.util.Iterator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.api.AEApi;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.AELog;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import appeng.util.iterators.InvIterator;

public class FakeAEInventory implements IInventory, Iterable<ItemStack> {

    private final IAEAppEngInventory te;
    private final IAEItemStack[] inv;
    private final int size;
    private int maxStack;

    public FakeAEInventory(final IAEAppEngInventory te, final int s) {
        this.te = te;
        this.size = s;
        this.maxStack = 64;
        this.inv = new IAEItemStack[s];
    }

    public boolean isEmpty() {
        for (int x = 0; x < this.size; x++) {
            if (this.getStackInSlot(x) != null) {
                return false;
            }
        }
        return true;
    }

    public void setMaxStackSize(final int s) {
        this.maxStack = s;
    }

    public IAEItemStack getAEStackInSlot(final int var1) {
        return this.inv[var1];
    }

    public void writeToNBT(final NBTTagCompound data, final String name) {
        final NBTTagCompound c = new NBTTagCompound();
        this.writeToNBT(c);
        data.setTag(name, c);
    }

    private void writeToNBT(final NBTTagCompound target) {
        for (int x = 0; x < this.size; x++) {
            try {
                final NBTTagCompound c = new NBTTagCompound();

                if (this.inv[x] != null) {
                    this.inv[x].writeToNBT(c);
                }

                target.setTag("#" + x, c);
            } catch (final Exception ignored) {}
        }
    }

    public void readFromNBT(final NBTTagCompound data, final String name) {
        final NBTTagCompound c = data.getCompoundTag(name);
        if (c != null) {
            this.readFromNBT(c);
        }
    }

    private void readFromNBT(final NBTTagCompound target) {
        for (int x = 0; x < this.size; x++) {
            try {
                final NBTTagCompound c = target.getCompoundTag("#" + x);

                if (c != null) {
                    this.inv[x] = AEItemStack.loadItemStackFromNBT(c);
                }
            } catch (final Exception e) {
                AELog.debug(e);
            }
        }
    }

    @Override
    public int getSizeInventory() {
        return this.size;
    }

    @Override
    public ItemStack getStackInSlot(final int var1) {
        if (this.inv[var1] == null) {
            return null;
        }

        return this.inv[var1].getItemStack();
    }

    @Override
    public ItemStack decrStackSize(final int slot, final int qty) {
        if (this.inv[slot] != null) {
            final ItemStack split = this.getStackInSlot(slot);
            ItemStack ns = null;

            if (qty >= split.stackSize) {
                ns = this.getStackInSlot(slot);
                this.inv[slot] = null;
            } else {
                ns = split.splitStack(qty);
            }

            if (this.te != null && Platform.isServer()) {
                this.te.onChangeInventory(this, slot, InvOperation.decreaseStackSize, ns, null);
            }

            return ns;
        }

        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(final int var1) {
        return null;
    }

    @Override
    public void setInventorySlotContents(final int slot, final ItemStack newItemStack) {
        final ItemStack oldStack = this.getStackInSlot(slot);
        this.inv[slot] = AEApi.instance()
            .storage()
            .createItemStack(newItemStack);

        if (this.te != null && Platform.isServer()) {
            ItemStack removed = oldStack;
            ItemStack added = newItemStack;

            if (oldStack != null && newItemStack != null && Platform.isSameItem(oldStack, newItemStack)) {
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

            this.te.onChangeInventory(this, slot, InvOperation.setInventorySlotContents, removed, added);
        }
    }

    public void setInventorySlotContentsNoCallBack(final int slot, final ItemStack newItemStack) {
        inv[slot] = AEApi.instance()
            .storage()
            .createItemStack(newItemStack);
    }

    @Override
    public String getInventoryName() {
        return "appeng-internal";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return this.maxStack > 64 ? 64 : this.maxStack;
    }

    @Override
    public void markDirty() {
        if (this.te != null && Platform.isServer()) {
            this.te.onChangeInventory(this, -1, InvOperation.markDirty, null, null);
        }
    }

    @Override
    public boolean isUseableByPlayer(final EntityPlayer var1) {
        return true;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(final int i, final ItemStack itemstack) {
        return true;
    }

    @Override
    public Iterator<ItemStack> iterator() {
        return new InvIterator(this);
    }

}
