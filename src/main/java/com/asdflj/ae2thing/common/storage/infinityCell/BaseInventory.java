package com.asdflj.ae2thing.common.storage.infinityCell;

import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.common.storage.DataStorage;

import appeng.api.implementations.guiobjects.IGuiItemObject;

public interface BaseInventory extends IGuiItemObject {

    default String getUUID() {
        return "";
    }

    default DataStorage getStorage() {
        return AE2ThingAPI.instance()
            .getStorageManager()
            .getStorage(this.getItemStack());
    }
}
