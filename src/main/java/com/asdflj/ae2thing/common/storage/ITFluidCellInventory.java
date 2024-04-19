package com.asdflj.ae2thing.common.storage;

import com.glodblock.github.common.storage.IFluidCellInventory;

public interface ITFluidCellInventory extends IFluidCellInventory {

    default String getUUID() {
        return "";
    }
}
