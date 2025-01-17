package com.asdflj.ae2thing.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;

import appeng.api.util.DimensionalCoord;

public class CellPos {

    private DimensionalCoord coord;
    private int slot;

    public CellPos(DimensionalCoord dimensionalCoord, int slot) {
        this.coord = dimensionalCoord;
        this.slot = slot;
    }

    public CellPos(int x, int y, int z, int dim, int slot) {
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

    public static void writeListToNBT(final NBTTagCompound tag, List<CellPos> list) {
        int i = 0;
        for (CellPos d : list) {
            NBTTagCompound data = new NBTTagCompound();
            writeToNBT(data, d.coord.x, d.coord.y, d.coord.z, d.coord.getDimension(), d.slot);
            tag.setTag("pos#" + i, data);
            i++;
        }
    }

    public static CellPos readFromNBT(final NBTTagCompound data) {
        return new CellPos(
            new DimensionalCoord(
                data.getInteger("x"),
                data.getInteger("y"),
                data.getInteger("z"),
                data.getInteger("dim")),
            data.getInteger("slot"));
    }

    public static List<CellPos> readAsListFromNBT(final NBTTagCompound tag) {
        List<CellPos> list = new ArrayList<>();
        int i = 0;
        while (tag.hasKey("pos#" + i)) {
            NBTTagCompound data = tag.getCompoundTag("pos#" + i);
            list.add(readFromNBT(data));
            i++;
        }
        return list;
    }
}
