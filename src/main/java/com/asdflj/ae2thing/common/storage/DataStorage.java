package com.asdflj.ae2thing.common.storage;

import java.util.UUID;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.asdflj.ae2thing.util.Util;

import appeng.api.AEApi;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.item.AEItemStack;

public class DataStorage implements IDataStorage {

    private final IItemList<IAEItemStack> items = AEApi.instance()
        .storage()
        .createItemList();
    private final IItemList<IAEFluidStack> fluids = AEApi.instance()
        .storage()
        .createFluidList();
    private final UUID uuid;
    private final StorageChannel channel;

    public DataStorage(UUID uuid, StorageChannel channel) {
        this.uuid = uuid;
        this.channel = channel;
    }

    @Override
    public StorageChannel getChannel() {
        return this.channel;
    }

    @Override
    public IItemList<IAEItemStack> getItems() {
        return items;
    }

    @Override
    public IItemList<IAEFluidStack> getFluids() {
        return fluids;
    }

    @Override
    public boolean isEmpty() {
        if (this.channel == StorageChannel.ITEMS) {
            return this.items.isEmpty();
        } else {
            return this.fluids.isEmpty();
        }
    }

    @Override
    public String getUUID() {
        return this.uuid.toString();
    }

    public static DataStorage readFromNBT(UUID uuid, NBTTagList data) {
        return readFromNBT(uuid, data, StorageChannel.ITEMS);
    }

    public static DataStorage readFromNBT(UUID uuid, NBTTagList data, StorageChannel channel) {
        DataStorage storage = new DataStorage(uuid, channel);
        storage.readFromNBT(data);
        return storage;
    }

    @Override
    public void readFromNBT(NBTTagList data) {
        if (this.channel == StorageChannel.ITEMS) {
            for (final IAEItemStack ais : this.readList(data)) {
                items.add(ais);
            }
        } else {
            for (final IAEFluidStack ais : this.readFluidList(data)) {
                fluids.add(ais);
            }
        }
    }

    private IItemList<IAEFluidStack> readFluidList(final NBTTagList tag) {
        final IItemList<IAEFluidStack> out = AEApi.instance()
            .storage()
            .createFluidList();
        if (tag == null) {
            return out;
        }
        for (int x = 0; x < tag.tagCount(); x++) {
            final IAEFluidStack ais = Util.loadFluidStackFromNBT(tag.getCompoundTagAt(x));
            if (ais != null) {
                out.add(ais);
            }
        }

        return out;
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
        if (this.channel == StorageChannel.ITEMS) {
            return writeList(items);
        } else {
            return writeFluidList(fluids);
        }
    }

    private NBTTagList writeFluidList(final IItemList<IAEFluidStack> myList) {
        final NBTTagList out = new NBTTagList();

        for (final IAEFluidStack ais : myList) {
            out.appendTag(this.writeFluid(ais));
        }

        return out;
    }

    private NBTBase writeFluid(IAEFluidStack fluid) {
        final NBTTagCompound out = new NBTTagCompound();

        if (fluid != null) {
            fluid.writeToNBT(out);
        }
        return out;
    }

    private NBTTagList writeList(final IItemList<IAEItemStack> myList) {
        final NBTTagList out = new NBTTagList();

        for (final IAEItemStack ais : myList) {
            out.appendTag(this.writeItem(ais));
        }

        return out;
    }

    private NBTTagCompound writeItem(final IAEItemStack item) {
        final NBTTagCompound out = new NBTTagCompound();

        if (item != null) {
            item.writeToNBT(out);
        }

        return out;
    }
}
