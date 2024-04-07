package com.asdflj.ae2thing.common.storage;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;

public interface IDataStorage {

    void readFromNBT(NBTTagList data);

    NBTBase writeToNBT();

    IItemList<IAEItemStack> getItems();

    boolean isEmpty();

    String getUUID();
}
