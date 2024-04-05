package com.asdflj.ae2thing.common.storage;

import com.asdflj.ae2thing.util.Ae2Reflect;

import appeng.api.config.IncludeExclude;
import appeng.api.storage.ICellCacheRegistry;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.me.storage.MEInventoryHandler;
import appeng.me.storage.MEPassThrough;
import appeng.util.prioitylist.FuzzyPriorityList;

public class CellInventoryHandler extends MEInventoryHandler<IAEItemStack>
    implements ICellInventoryHandler, ICellCacheRegistry {

    public CellInventoryHandler(IMEInventory<IAEItemStack> i) {
        super(i, StorageChannel.ITEMS);
    }

    @Override
    public boolean canGetInv() {
        return this.getCellInv() != null;
    }

    @Override
    public long getTotalBytes() {
        return this.getCellInv()
            .getTotalBytes();
    }

    @Override
    public long getFreeBytes() {
        return this.getCellInv()
            .getFreeBytes();
    }

    @Override
    public long getUsedBytes() {
        return this.getCellInv()
            .getUsedBytes();
    }

    @Override
    public long getTotalTypes() {
        return this.getCellInv()
            .getTotalItemTypes();
    }

    @Override
    public long getFreeTypes() {
        return this.getCellInv()
            .getRemainingItemTypes();
    }

    @Override
    public long getUsedTypes() {
        return this.getCellInv()
            .getStoredItemTypes();
    }

    @Override
    public int getCellStatus() {
        return this.getStatusForCell();
    }

    @Override
    public TYPE getCellType() {
        return TYPE.ITEM;
    }

    @Override
    public ITCellInventory getCellInv() {
        Object o = this.getInternal();
        if (o instanceof MEPassThrough) {
            o = Ae2Reflect.getInternal((MEPassThrough<?>) o);
        }
        return (ITCellInventory) (o instanceof ITCellInventory ? o : null);
    }

    @Override
    public boolean isPreformatted() {
        return !Ae2Reflect.getPartitionList(this)
            .isEmpty();
    }

    @Override
    public boolean isFuzzy() {
        return Ae2Reflect.getPartitionList(this) instanceof FuzzyPriorityList;
    }

    @Override
    public IncludeExclude getIncludeExcludeMode() {
        return this.getWhitelist();
    }

    public int getStatusForCell() {
        int val = this.getCellInv()
            .getStatusForCell();

        if (val == 1 && this.isPreformatted()) {
            val = 2;
        }

        return val;
    }
}
