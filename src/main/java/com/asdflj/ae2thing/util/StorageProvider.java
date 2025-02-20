package com.asdflj.ae2thing.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.util.DimensionalCoord;

public class StorageProvider {

    private DimensionalCoord coord;
    private int slot;

    private StorageProvider(DimensionalCoord dimensionalCoord, int slot, ForgeDirection face) {
        if (face != ForgeDirection.UNKNOWN) {
            this.coord = offset(dimensionalCoord, face);
        } else {
            this.coord = dimensionalCoord;
        }
        this.slot = slot;

    }

    public static DimensionalCoord offset(DimensionalCoord d, ForgeDirection face) {
        return new DimensionalCoord(d.getWorld(), d.x + face.offsetX, d.y + face.offsetY, d.z + face.offsetZ);
    }

    public StorageProvider(DimensionalCoord dimensionalCoord, ForgeDirection face) {
        this(dimensionalCoord, -1, face);
    }

    public StorageProvider(DimensionalCoord dimensionalCoord, int slot) {
        this(dimensionalCoord, slot, ForgeDirection.UNKNOWN);
    }

    public StorageProvider(int x, int y, int z, int dim, int slot) {
        this(new DimensionalCoord(x, y, z, dim), slot);
    }

    public int getSlot() {
        return slot;
    }

    public DimensionalCoord getCoord() {
        return coord;
    }

    public void writeToNBT(NBTTagCompound tag) {
        writeToNBT(tag, this.coord.x, this.coord.y, this.coord.z, this.coord.getDimension(), this.slot);
    }

    private static void writeToNBT(final NBTTagCompound data, int x, int y, int z, int dimId, int slot) {
        data.setInteger("dim", dimId);
        data.setInteger("x", x);
        data.setInteger("y", y);
        data.setInteger("z", z);
        data.setInteger("slot", slot);
    }

    public static void writeListToNBT(final NBTTagCompound tag, List<StorageProvider> list) {
        int i = 0;
        for (StorageProvider d : list) {
            NBTTagCompound data = new NBTTagCompound();
            writeToNBT(data, d.coord.x, d.coord.y, d.coord.z, d.coord.getDimension(), d.slot);
            tag.setTag("pos#" + i, data);
            i++;
        }
    }

    public static StorageProvider readFromNBT(final NBTTagCompound data) {
        return new StorageProvider(
            new DimensionalCoord(
                data.getInteger("x"),
                data.getInteger("y"),
                data.getInteger("z"),
                data.getInteger("dim")),
            data.getInteger("slot"));
    }

    public static List<StorageProvider> readAsListFromNBT(final NBTTagCompound tag) {
        List<StorageProvider> list = new ArrayList<>();
        int i = 0;
        while (tag.hasKey("pos#" + i)) {
            NBTTagCompound data = tag.getCompoundTag("pos#" + i);
            list.add(readFromNBT(data));
            i++;
        }
        return list;
    }
}
