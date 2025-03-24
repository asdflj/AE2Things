package com.asdflj.ae2thing.common.storage.infinityCell;

import java.util.Objects;

import javax.annotation.Nonnull;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.common.item.BaseCellItem;
import com.asdflj.ae2thing.common.storage.ITCellInventory;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.exceptions.AppEngException;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.item.AEItemStack;

public class CreativeCellInventory implements ITCellInventory {

    private IItemList<IAEItemStack> itemListCache = null;
    private final ItemStack cellItem;
    private final IStorageCell cellType;

    public CreativeCellInventory(final ItemStack o) throws AppEngException {
        if (o == null) {
            throw new AppEngException("ItemStack was used as a cell, but was not a cell!");
        }
        this.cellItem = o;
        this.cellType = (IStorageCell) this.cellItem.getItem();
    }

    protected IItemList<IAEItemStack> getCellItems() {
        if (this.itemListCache == null) {
            this.loadCellItems();
        }
        return this.itemListCache;
    }

    @Override
    public double getIdleDrain(ItemStack is) {
        return this.cellType.getIdleDrain(is);
    }

    public void loadCellItems() {
        if (this.itemListCache == null) {
            this.itemListCache = AEApi.instance()
                .storage()
                .createPrimitiveItemList();
        }
        this.itemListCache.resetStatus(); // clears totals and stuff.
        IInventory inv = this.cellType.getConfigInventory(this.cellItem);
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            IAEItemStack is = AEItemStack.create(inv.getStackInSlot(i));
            if (is != null) {
                is.setStackSize(Integer.MAX_VALUE * 1000L);
                this.itemListCache.add(is);
            }
        }
    }

    @Override
    public ItemStack getItemStack() {
        return this.cellItem;
    }

    @Override
    public FuzzyMode getFuzzyMode() {
        return this.cellType.getFuzzyMode(this.cellItem);
    }

    @Override
    public IInventory getConfigInventory() {
        return this.cellType.getConfigInventory(this.cellItem);
    }

    @Override
    public IInventory getUpgradesInventory() {
        return this.cellType.getUpgradesInventory(this.cellItem);
    }

    @Override
    public int getBytesPerType() {
        return this.cellType.getBytesPerType(this.cellItem);
    }

    @Override
    public boolean canHoldNewItem(ItemStack is) {
        return false;
    }

    @Override
    public long getTotalBytes() {
        return this.cellType.getTotalTypes(this.cellItem);
    }

    @Override
    public long getFreeBytes() {
        return 0;
    }

    @Override
    public long getUsedBytes() {
        return 0;
    }

    @Override
    public long getTotalItemTypes() {
        return this.cellType.getTotalTypes(this.cellItem);
    }

    @Override
    public long getStoredItemCount() {
        return Integer.MAX_VALUE * 1000L;
    }

    @Override
    public long getStoredItemTypes() {
        return 1;
    }

    @Override
    public long getRemainingItemTypes() {
        return 0;
    }

    @Override
    public long getRemainingItemCount() {
        return 0;
    }

    @Override
    public int getUnusedItemCount() {
        return 0;
    }

    @Override
    public int getStatusForCell() {
        if (this.canHoldNewItem(this.cellItem)) {
            return 1;
        }
        if (this.getRemainingItemCount() > 0) {
            return 2;
        }
        return 3;
    }

    @Override
    public String getOreFilter() {
        return this.cellType.getOreFilter(this.cellItem);
    }

    @Override
    public IAEItemStack injectItems(final IAEItemStack input, final Actionable mode, final BaseActionSource src) {
        if (input == null || input.getStackSize() == 0) {
            return null;
        }
        if (this.cellType.isBlackListed(this.cellItem, input)) {
            return input;
        }
        if (this.getCellItems()
            .findPrecise(input) != null) {
            return null;
        } else {
            return input;
        }
    }

    @Override
    public IAEItemStack extractItems(final IAEItemStack request, final Actionable mode, final BaseActionSource src) {
        if (request == null) {
            return null;
        }
        if (this.getCellItems()
            .findPrecise(request) != null) {
            return request.copy();
        } else {
            return null;
        }
    }

    @Override
    public IItemList<IAEItemStack> getAvailableItems(final IItemList<IAEItemStack> out, int iteration) {
        for (final IAEItemStack ais : this.getCellItems()) {
            out.add(ais);
        }
        return out;
    }

    @Override
    public IAEItemStack getAvailableItem(@Nonnull IAEItemStack request, int iteration) {
        final IAEItemStack local = this.getCellItems()
            .findPrecise(request);
        if (local == null) {
            return null;
        }

        return request.copy();
    }

    @Override
    public StorageChannel getChannel() {
        return ((BaseCellItem) Objects.requireNonNull(this.cellItem.getItem())).getChannel();
    }
}
