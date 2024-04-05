package com.asdflj.ae2thing.common.storage;

import appeng.api.config.IncludeExclude;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.data.IAEItemStack;

public interface ICellInventoryHandler extends IMEInventoryHandler<IAEItemStack> {

    ITCellInventory getCellInv();

    boolean isPreformatted();

    boolean isFuzzy();

    IncludeExclude getIncludeExcludeMode();
}
