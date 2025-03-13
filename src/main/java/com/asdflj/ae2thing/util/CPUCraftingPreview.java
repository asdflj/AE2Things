package com.asdflj.ae2thing.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;

import com.asdflj.ae2thing.api.Constants;

import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;

public class CPUCraftingPreview {

    public final String name;
    public final long remainingItemCount;
    public final List<IAEItemStack> itemList;
    public final long elapsedTime;
    public static final int maxSize = 5;

    public CPUCraftingPreview(String name, long remainingItemCount, long elapsedTime, List<IAEItemStack> list) {
        this.name = name;
        this.remainingItemCount = remainingItemCount;
        this.itemList = list;
        this.elapsedTime = elapsedTime;
    }

    public void writeToNBT(NBTTagCompound data) {
        data.setString(Constants.NAME, this.name);
        data.setLong(Constants.REMAINING_ITEM_COUNT, this.remainingItemCount);
        data.setLong(Constants.CPU_ELAPSED_TIME, this.elapsedTime);
        data.setInteger(Constants.SIZE, this.itemList.size());
        for (int i = 0; i < this.itemList.size(); i++) {
            IAEItemStack item = this.itemList.get(i);
            NBTTagCompound tag = new NBTTagCompound();
            item.writeToNBT(tag);
            data.setTag("#" + i, tag);
        }
    }

    public static CPUCraftingPreview readFromNBT(NBTTagCompound data) {
        String name = data.getString(Constants.NAME);
        long remainingItemCount = data.getLong(Constants.REMAINING_ITEM_COUNT);
        long elapsedTime = data.getLong(Constants.CPU_ELAPSED_TIME);
        int size = data.getInteger(Constants.SIZE);
        List<IAEItemStack> itemList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            NBTTagCompound tag = data.getCompoundTag("#" + i);
            IAEItemStack item = AEItemStack.loadItemStackFromNBT(tag);
            itemList.add(item);
        }
        return new CPUCraftingPreview(name, remainingItemCount, elapsedTime, itemList);
    }
}
