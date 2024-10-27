package com.asdflj.ae2thing.common.storage;

import java.util.UUID;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.glodblock.github.util.Util;

import appeng.api.AEApi;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.item.AEItemStack;

public class DataStorage implements IDataStorage {

    private IItemList<IAEItemStack> items;
    private IItemList<IAEFluidStack> fluids;
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
        if (this.items == null) {
            this.items = AEApi.instance()
                .storage()
                .createPrimitiveItemList();
        }
        return items;
    }

    @Override
    public IItemList<IAEFluidStack> getFluids() {
        if (this.fluids == null) {
            this.fluids = AEApi.instance()
                .storage()
                .createFluidList();
        }
        return fluids;
    }

    @Override
    public boolean isEmpty() {
        if (this.getChannel() == StorageChannel.ITEMS) {
            return this.getItems()
                .isEmpty();
        } else {
            return this.getFluids()
                .isEmpty();
        }
    }

    @Override
    public String getUUID() {
        return this.uuid.toString();
    }

    @Override
    public UUID getRawUUID() {
        return this.uuid;
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
        if (this.getChannel() == StorageChannel.ITEMS) {
            for (final IAEItemStack ais : this.readList(data)) {
                this.getItems()
                    .add(ais);
            }
        } else {
            for (final IAEFluidStack ais : this.readFluidList(data)) {
                this.getFluids()
                    .add(ais);
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
        if (this.getChannel() == StorageChannel.ITEMS) {
            return writeList(this.getItems());
        } else {
            return writeFluidList(this.getFluids());
        }
    }

    private NBTTagList writeFluidList(final IItemList<IAEFluidStack> myList) {
        final NBTTagList out = new NBTTagList();

        for (final IAEFluidStack ais : myList) {
            if (ais.getStackSize() > 0) {
                out.appendTag(this.writeFluid(ais));
            }
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
            if (ais.getStackSize() > 0) {
                out.appendTag(this.writeItem(ais));
            }
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
