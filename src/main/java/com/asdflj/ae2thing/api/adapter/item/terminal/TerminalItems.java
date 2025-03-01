package com.asdflj.ae2thing.api.adapter.item.terminal;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class TerminalItems {

    private ItemStack raw;
    private ItemStack target;
    private IItemTerminal handler = null;

    public TerminalItems(ItemStack raw, ItemStack target) {
        this(raw, target, null);
    }

    public TerminalItems(ItemStack raw, ItemStack target, IItemTerminal handler) {
        this.raw = raw;
        this.target = target;
        this.handler = handler;
    }

    public ItemStack getRawItem() {
        return raw;
    }

    public ItemStack getTargetItem() {
        return target;
    }

    public void setRawItem(ItemStack raw) {
        this.raw = raw;
    }

    public void setTargetItem(ItemStack target) {
        this.target = target;
    }

    public void writeNBT(NBTTagCompound tag) {
        NBTTagCompound raw = new NBTTagCompound();
        NBTTagCompound target = new NBTTagCompound();
        getRawItem().writeToNBT(raw);
        getTargetItem().writeToNBT(target);
        tag.setTag("#0", raw);
        tag.setTag("#1", target);
        if (this.handler != null) {
            this.handler.run(this);
        }

    }

    public static TerminalItems readFromNBT(NBTTagCompound tag) {
        ItemStack raw = ItemStack.loadItemStackFromNBT((NBTTagCompound) tag.getTag("#0"));
        ItemStack target = ItemStack.loadItemStackFromNBT((NBTTagCompound) tag.getTag("#1"));
        return new TerminalItems(raw, target);
    }
}
