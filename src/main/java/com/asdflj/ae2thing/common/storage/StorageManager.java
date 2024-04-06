package com.asdflj.ae2thing.common.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.WorldSavedData;

import com.asdflj.ae2thing.api.AE2ThingAPI;

public class StorageManager extends WorldSavedData {

    private Map<UUID, DataStorage> disks = new HashMap<>();

    public StorageManager(String name) {
        super(name);
        this.setDirty(true);
        AE2ThingAPI.instance()
            .setStorageManager(this);
    }

    public DataStorage getStorage(String uuid) {
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
            d = new DataStorage(uid);
            disks.put(uid, d);
        }
        return d;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        Map<UUID, DataStorage> d = new HashMap<>();
        NBTTagList diskList = data.getTagList(Constants.DISKLIST, 10);
        for (int i = 0; i < diskList.tagCount(); i++) {
            NBTTagCompound disk = diskList.getCompoundTagAt(i);
            UUID uid = UUID.fromString(disk.getString(Constants.DISKUUID));
            d.put(uid, DataStorage.readFromNBT(uid, disk.getTagList(Constants.DISKDATA, 10)));
        }
        disks = d;
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        NBTTagList diskList = new NBTTagList();
        for (Map.Entry<UUID, DataStorage> entry : disks.entrySet()) {
            NBTTagCompound disk = new NBTTagCompound();

            disk.setString(
                Constants.DISKUUID,
                entry.getKey()
                    .toString());
            disk.setTag(
                Constants.DISKDATA,
                entry.getValue()
                    .writeToNBT());
            diskList.appendTag(disk);
        }
        data.setTag(Constants.DISKLIST, diskList);
    }
}
