package com.asdflj.ae2thing.common.storage.infinityCell;

import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.common.storage.DataStorage;

import appeng.api.storage.StorageChannel;

public interface BaseInventory {

    StorageChannel getChannel();

    default String getUUID() {
        return "";
    }

    default DataStorage getStorage() {
        String uuid = this.getUUID();
        return AE2ThingAPI.instance()
            .getStorageManager()
            .getStorage(uuid, this.getChannel());
    }
}
