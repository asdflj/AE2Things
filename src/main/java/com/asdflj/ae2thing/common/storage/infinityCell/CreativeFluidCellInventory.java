package com.asdflj.ae2thing.common.storage.infinityCell;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.asdflj.ae2thing.common.item.BaseCellItem;
import com.asdflj.ae2thing.common.storage.ITFluidCellInventory;
import com.glodblock.github.common.storage.IStorageFluidCell;
import com.glodblock.github.util.Util;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.exceptions.AppEngException;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;

public class CreativeFluidCellInventory implements ITFluidCellInventory {

    protected IStorageFluidCell cellType;
    protected final ItemStack cellItem;
    protected final ISaveProvider container;
    protected IItemList<IAEFluidStack> cellFluids = null;

    public CreativeFluidCellInventory(ItemStack o, ISaveProvider c, EntityPlayer player) throws AppEngException {
        if (o == null) {
            throw new AppEngException("ItemStack was used as a cell, but was not a cell!");
        }
        this.cellItem = o;
        this.cellType = (IStorageFluidCell) this.cellItem.getItem();
        this.container = c;
    }

    @Override
    public ItemStack getItemStack() {
        return this.cellItem;
    }

    @Override
    public double getIdleDrain(ItemStack is) {
        return this.cellType.getIdleDrain(is);
    }

    @Override
    public IInventory getConfigInventory() {
        return this.cellType.getConfigInventory(this.cellItem);
    }

    @Override
    public int getBytesPerType() {
        return this.cellType.getBytesPerType(this.cellItem);
    }

    @Override
    public boolean canHoldNewFluid() {
        return false;
    }

    @Override
    public long getTotalBytes() {
        return this.cellType.getBytes(this.cellItem);
    }

    @Override
    public long getFreeBytes() {
        return 0;
    }

    @Override
    public long getUsedBytes() {
        return Long.MAX_VALUE;
    }

    @Override
    public long getStoredFluidCount() {
        return 0;
    }

    @Override
    public long getRemainingFluidCount() {
        return 0;
    }

    @Override
    public long getRemainingFluidTypes() {
        return 0;
    }

    @Override
    public int getUnusedFluidCount() {
        return 0;
    }

    @Override
    public int getStatusForCell() {
        if (this.canHoldNewFluid()) {
            return 1;
        }
        if (this.getRemainingFluidCount() > 0) {
            return 2;
        }
        return 3;
    }

    @Override
    public long getStoredFluidTypes() {
        return 1;
    }

    @Override
    public long getTotalFluidTypes() {
        return 1;
    }

    @Override
    public List<IAEFluidStack> getContents() {
        List<IAEFluidStack> ret = new ArrayList<>();
        for (IAEFluidStack fluid : this.getCellFluids()) {
            ret.add(fluid);
        }
        return ret;
    }

    @Override
    public IInventory getUpgradesInventory() {
        return this.cellType.getUpgradesInventory(this.cellItem);
    }

    @Override
    public IAEFluidStack injectItems(IAEFluidStack input, Actionable type, BaseActionSource src) {
        if (input == null || input.getStackSize() == 0) {
            return null;
        }
        if (this.cellType.isBlackListed(this.cellItem, input)) {
            return input;
        }
        if (this.getCellFluids()
            .findPrecise(input) != null) {
            return null;
        } else {
            return input;
        }
    }

    @Override
    public IAEFluidStack extractItems(IAEFluidStack request, Actionable mode, BaseActionSource src) {
        if (request == null) {
            return null;
        }
        if (this.getCellFluids()
            .findPrecise(request) != null) {
            return request.copy();
        } else {
            return null;
        }
    }

    @Override
    public IItemList<IAEFluidStack> getAvailableItems(IItemList<IAEFluidStack> out) {
        for (final IAEFluidStack i : this.getCellFluids()) {
            out.add(i);
        }
        return out;
    }

    protected IItemList<IAEFluidStack> getCellFluids() {
        if (this.cellFluids == null) {
            this.loadCellFluids();
        }
        return this.cellFluids;
    }

    protected void loadCellFluids() {
        if (this.cellFluids == null) {
            this.cellFluids = AEApi.instance()
                .storage()
                .createFluidList();
        }
        this.cellFluids.resetStatus(); // clears totals and stuff.
        IInventory inv = this.cellType.getConfigInventory(this.cellItem);
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack is = inv.getStackInSlot(i);
            if (Util.FluidUtil.isFluidContainer(is)) {
                FluidStack fs = Util.getFluidFromItem(is);
                if (fs == null) continue;
                IAEFluidStack iaeFluidStack = Util.FluidUtil.createAEFluidStack(fs);
                if (this.cellFluids.findPrecise(iaeFluidStack) == null) {
                    iaeFluidStack.setStackSize(Integer.MAX_VALUE * 1000L);
                    this.cellFluids.add(iaeFluidStack);
                }
            }
        }
    }

    @Override
    public StorageChannel getChannel() {
        return ((BaseCellItem) Objects.requireNonNull(this.cellItem.getItem())).getChannel();
    }
}
