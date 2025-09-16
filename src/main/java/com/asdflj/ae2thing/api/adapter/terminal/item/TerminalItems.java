package com.asdflj.ae2thing.api.adapter.terminal.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.util.Platform;

public class TerminalItems {

    private ItemStack raw;
    private ItemStack target;
    private String displayName;
    private NBTTagCompound data;

    public TerminalItems(ItemStack raw, ItemStack target) {
        this(raw, target, Platform.getItemDisplayName(target), new NBTTagCompound());
    }

    public TerminalItems(ItemStack raw, ItemStack target, NBTTagCompound data) {
        this(raw, target, Platform.getItemDisplayName(target), data);
    }

    public TerminalItems(ItemStack raw, ItemStack target, String displayName, NBTTagCompound data) {
        this.raw = raw;
        this.target = target;
        this.displayName = displayName;
        this.data = data;
    }

    public NBTTagCompound getData() {
        return data;
    }

    public void setData(NBTTagCompound data) {
        this.data = data;
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

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void writeNBT(NBTTagCompound tag) {
        NBTTagCompound raw = new NBTTagCompound();
        NBTTagCompound target = new NBTTagCompound();
        getRawItem().writeToNBT(raw);
        getTargetItem().writeToNBT(target);
        tag.setTag("#0", raw);
        tag.setTag("#1", target);
        tag.setString("displayName", displayName);
        tag.setTag("data", data);
    }

    public static TerminalItems readFromNBT(NBTTagCompound tag) {
        ItemStack raw = ItemStack.loadItemStackFromNBT((NBTTagCompound) tag.getTag("#0"));
        ItemStack target = ItemStack.loadItemStackFromNBT((NBTTagCompound) tag.getTag("#1"));
        NBTTagCompound data = tag.getCompoundTag("data");
        return new TerminalItems(raw, target, tag.getString("displayName"), data);
    }
}
