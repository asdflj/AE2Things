package com.asdflj.ae2thing.common.storage;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;

import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;

public interface IDataStorage {

    void readFromNBT(NBTTagList data);

    NBTBase writeToNBT();

    IItemList<IAEItemStack> getItems();

    IItemList<IAEFluidStack> getFluids();

    boolean isEmpty();

    String getUUID();

    StorageChannel getChannel();
}
