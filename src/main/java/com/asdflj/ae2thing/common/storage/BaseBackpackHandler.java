package com.asdflj.ae2thing.common.storage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public abstract class BaseBackpackHandler implements IInventory {

    protected final IInventory inv;

    public BaseBackpackHandler(IInventory inv) {
        this.inv = inv;
    }

    @Override
    public int getSizeInventory() {
        return inv.getSizeInventory();
    }

    @Override
    public ItemStack getStackInSlot(int slotIn) {
        return this.inv.getStackInSlot(slotIn);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return this.inv.decrStackSize(index, count);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        return this.inv.getStackInSlotOnClosing(index);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        this.inv.setInventorySlotContents(index, stack);
    }

    @Override
    public String getInventoryName() {
        return this.inv.getInventoryName();
    }

    @Override
    public boolean hasCustomInventoryName() {
        return this.inv.hasCustomInventoryName();
    }

    @Override
    public int getInventoryStackLimit() {
        return this.inv.getInventoryStackLimit();
    }

    @Override
    public void markDirty() {
        this.inv.markDirty();
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return this.inv.isUseableByPlayer(player);
    }

    @Override
    public void openInventory() {
        this.inv.openInventory();
    }

    @Override
    public void closeInventory() {
        this.inv.closeInventory();
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return this.inv.isItemValidForSlot(index, stack);
    }
}
