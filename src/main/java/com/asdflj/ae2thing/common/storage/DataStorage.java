package com.asdflj.ae2thing.common.storage;

import java.util.UUID;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import appeng.api.AEApi;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.item.AEItemStack;

public class DataStorage implements IDataStorage {

    private final IItemList<IAEItemStack> items = AEApi.instance()
        .storage()
        .createItemList();
    private final UUID uuid;

    public DataStorage(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public IItemList<IAEItemStack> getItems() {
        return items;
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public String getUUID() {
        return this.uuid.toString();
    }

    public static DataStorage readFromNBT(UUID uuid, NBTTagList data) {
        DataStorage storage = new DataStorage(uuid);
        storage.readFromNBT(data);
        return storage;
    }

    @Override
    public void readFromNBT(NBTTagList data) {
        for (final IAEItemStack ais : this.readList(data)) {
            items.add(ais);
        }
    }

    private IItemList<IAEItemStack> readList(final NBTTagList tag) {
        final IItemList<IAEItemStack> out = AEApi.instance()
            .storage()
            .createItemList();

        if (tag == null) {
            return out;
        }

        for (int x = 0; x < tag.tagCount(); x++) {
            final IAEItemStack ais = AEItemStack.loadItemStackFromNBT(tag.getCompoundTagAt(x));
            if (ais != null) {
                out.add(ais);
            }
        }

        return out;
    }

    @Override
    public NBTBase writeToNBT() {
        return writeList(items);
    }

    private NBTTagList writeList(final IItemList<IAEItemStack> myList) {
        final NBTTagList out = new NBTTagList();

        for (final IAEItemStack ais : myList) {
            out.appendTag(this.writeItem(ais));
        }

        return out;
    }

    private NBTTagCompound writeItem(final IAEItemStack finalOutput2) {
        final NBTTagCompound out = new NBTTagCompound();

        if (finalOutput2 != null) {
            finalOutput2.writeToNBT(out);
        }

        return out;
    }
}
