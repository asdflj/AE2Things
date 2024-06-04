package com.asdflj.ae2thing.common.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.WorldSavedData;

import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.common.item.BaseCellItem;

import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.networking.IGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.StorageChannel;
import appeng.tile.grid.AENetworkInvTile;
import appeng.util.Platform;

public class StorageManager extends WorldSavedData {

    private Map<UUID, DataStorage> disks = new HashMap<>();
    private final Map<UUID, Set<IGrid>> grids = new HashMap<>();

    public StorageManager(String name) {
        super(name);
        this.setDirty(true);
        AE2ThingAPI.instance()
            .setStorageManager(this);
    }

    private DataStorage getStorage(String uuid, StorageChannel channel) {
        UUID uid;
        DataStorage d;
        try {
            uid = UUID.fromString(uuid);
        } catch (Exception ignored) {
            do {
                uid = UUID.randomUUID();
            } while (disks.get(uid) != null);
        }
        d = disks.get(uid);
        if (d == null) {
            d = new DataStorage(uid, channel);
            disks.put(uid, d);
        }
        return d;
    }

    public void addGrid(String uuid, IGrid grid) {
        if (grid.isEmpty()) return;
        UUID uid = UUID.fromString(uuid);
        this.grids.putIfAbsent(uid, new HashSet<>());
        this.grids.get(uid)
            .add(grid);
    }

    public void addGrid(String uuid, IChestOrDrive drive) {
        if (drive == null) return;
        this.addGrid(
            uuid,
            drive.getActionableNode()
                .getGrid());
    }

    public DataStorage getStorage(ItemStack item) {
        if (item.getItem() instanceof BaseCellItem bci) {
            NBTTagCompound data = Platform.openNbtData(item);
            return this.getStorage(data.getString(Constants.DISKUUID), bci.getChannel());
        }
        return null;
    }

    public void setStorage(String uuid, ItemStack target) {
        NBTTagCompound data = Platform.openNbtData(target);
        String curUid = data.getString(Constants.DISKUUID);
        if (!curUid.isEmpty() && !curUid.equals(uuid)) {
            this.disks.remove(UUID.fromString(curUid));
        }
        data.setString(Constants.DISKUUID, uuid);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        Map<UUID, DataStorage> d = new HashMap<>();
        NBTTagList diskList = data.getTagList(Constants.DISKLIST, 10);
        for (int i = 0; i < diskList.tagCount(); i++) {
            NBTTagCompound disk = diskList.getCompoundTagAt(i);
            UUID uid = UUID.fromString(disk.getString(Constants.DISKUUID));
            d.put(uid, DataStorage.readFromNBT(uid, disk.getTagList(Constants.DISKDATA, 10), StorageChannel.ITEMS));
        }
        NBTTagList fluidDiskList = data.getTagList(Constants.FLUID_DISKLIST, 10);
        for (int i = 0; i < fluidDiskList.tagCount(); i++) {
            NBTTagCompound disk = fluidDiskList.getCompoundTagAt(i);
            UUID uid = UUID.fromString(disk.getString(Constants.DISKUUID));
            d.put(
                uid,
                DataStorage.readFromNBT(uid, disk.getTagList(Constants.FLUID_DISKLIST, 10), StorageChannel.FLUIDS));
        }
        disks = d;
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        NBTTagList diskList = new NBTTagList();
        NBTTagList fluidDiskList = new NBTTagList();
        for (Map.Entry<UUID, DataStorage> entry : disks.entrySet()) {
            if (entry.getValue()
                .isEmpty()) continue;
            NBTTagCompound disk = new NBTTagCompound();

            disk.setString(
                Constants.DISKUUID,
                entry.getKey()
                    .toString());
            if (entry.getValue()
                .getChannel() == StorageChannel.ITEMS) {
                disk.setTag(
                    Constants.DISKDATA,
                    entry.getValue()
                        .writeToNBT());
                diskList.appendTag(disk);
            } else {
                disk.setTag(
                    Constants.FLUID_DISKLIST,
                    entry.getValue()
                        .writeToNBT());
                fluidDiskList.appendTag(disk);
            }

        }
        data.setTag(Constants.DISKLIST, diskList);
        data.setTag(Constants.FLUID_DISKLIST, fluidDiskList);
    }

    public void postChanges(final ItemStack cell, final DataStorage storage, IChestOrDrive drive) {
        if (drive instanceof AENetworkInvTile ait) {
            try {
                IGrid curGrid = ait.getProxy()
                    .getGrid();
                UUID uid = UUID.fromString(storage.getUUID());
                if (curGrid.isEmpty()) return;
                Set<IGrid> iGrids = this.grids.get(uid);
                if (iGrids == null || iGrids.size() <= 1) return;
                List<IGrid> cp = new ArrayList<>(iGrids);
                iGrids.removeIf(i -> i != curGrid);
                for (IGrid grid : cp) {
                    if (grid.isEmpty() || grid.equals(curGrid)) continue;
                    IStorageGrid iStorageGrid = grid.getCache(IStorageGrid.class);
                    if (storage.getChannel() == StorageChannel.ITEMS) {
                        iStorageGrid.postAlterationOfStoredItems(storage.getChannel(), storage.getItems(), null);
                    } else if (storage.getChannel() == StorageChannel.FLUIDS) {
                        iStorageGrid.postAlterationOfStoredItems(storage.getChannel(), storage.getFluids(), null);
                    }
                }
            } catch (Exception ignored) {}
        }
    }
}
