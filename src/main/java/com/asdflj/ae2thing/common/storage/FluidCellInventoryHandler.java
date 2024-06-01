package com.asdflj.ae2thing.common.storage;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.util.Ae2Reflect;
import com.asdflj.ae2thing.util.Util;

import appeng.api.AEApi;
import appeng.api.config.IncludeExclude;
import appeng.api.storage.ICellCacheRegistry;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.me.storage.MEInventoryHandler;
import appeng.me.storage.MEPassThrough;
import appeng.util.item.AEFluidStack;
import appeng.util.prioitylist.PrecisePriorityList;

public class FluidCellInventoryHandler extends MEInventoryHandler<IAEFluidStack>
    implements ITFluidCellInventoryHandler, ICellCacheRegistry {

    public FluidCellInventoryHandler(final IMEInventory<IAEFluidStack> c) {
        super(c, StorageChannel.FLUIDS);

        final ITFluidCellInventory ci = this.getCellInv();

        if (ci != null) {
            final IInventory config = ci.getConfigInventory();

            final IItemList<IAEFluidStack> priorityList = AEApi.instance()
                .storage()
                .createFluidList();
            for (int x = 0; x < config.getSizeInventory(); x++) {
                final ItemStack is = config.getStackInSlot(x);
                if (Util.getFluidFromItem(is) != null) {
                    priorityList.add(AEFluidStack.create(Util.getFluidFromItem(is)));
                }
            }
            if (!priorityList.isEmpty()) {
                this.setPartitionList(new PrecisePriorityList<>(priorityList));
            }
        }
    }

    @Override
    public ITFluidCellInventory getCellInv() {
        Object o = this.getInternal();
        if (o instanceof MEPassThrough) {
            o = Ae2Reflect.getInternal((MEPassThrough<?>) o);
        }
        return (ITFluidCellInventory) (o instanceof ITFluidCellInventory ? o : null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterable<IAEFluidStack> getPartitionInv() {
        return (Iterable<IAEFluidStack>) Ae2Reflect.getPartitionList(this)
            .getItems();
    }

    @Override
    public boolean isPreformatted() {
        return !Ae2Reflect.getPartitionList(this)
            .isEmpty();
    }

    @Override
    public IncludeExclude getIncludeExcludeMode() {
        return IncludeExclude.WHITELIST;
    }

    public int getStatusForCell() {
        int val = this.getCellInv()
            .getStatusForCell();

        if (val == 1 && this.isPreformatted()) {
            val = 2;
        }

        return val;
    }

    @Override
    public boolean canGetInv() {
        return true;
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
            .getTotalFluidTypes();
    }

    @Override
    public long getFreeTypes() {
        return this.getCellInv()
            .getRemainingFluidTypes();
    }

    @Override
    public long getUsedTypes() {
        return this.getCellInv()
            .getStoredFluidTypes();
    }

    @Override
    public int getCellStatus() {
        return this.getStatusForCell();
    }

    @Override
    public TYPE getCellType() {
        return TYPE.FLUID;
    }
}
