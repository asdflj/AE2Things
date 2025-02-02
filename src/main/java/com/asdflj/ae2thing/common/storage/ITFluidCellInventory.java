package com.asdflj.ae2thing.common.storage;

import com.asdflj.ae2thing.common.storage.infinityCell.BaseInventory;
import com.glodblock.github.common.storage.IFluidCellInventory;

import appeng.api.storage.data.IAEFluidStack;

public interface ITFluidCellInventory extends IFluidCellInventory, BaseInventory {

    // backward compatibility
    default long getRemainingFluidCountDist(IAEFluidStack l) {
        return 0;
    }
}
