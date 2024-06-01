package com.asdflj.ae2thing.common.storage.infinityCell;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.common.item.ItemInfinityFluidCell;
import com.asdflj.ae2thing.common.storage.Constants;
import com.asdflj.ae2thing.common.storage.DataStorage;
import com.asdflj.ae2thing.common.storage.FluidCellInventoryHandler;
import com.asdflj.ae2thing.common.storage.ITFluidCellInventory;
import com.glodblock.github.common.storage.IStorageFluidCell;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.exceptions.AppEngException;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.util.Platform;

public class InfinityFluidCellInventory implements ITFluidCellInventory {

    protected static final String FLUID_TYPE_TAG = "ft";
    protected static final String FLUID_COUNT_TAG = "fc";
    protected IStorageFluidCell cellType;
    protected final ItemStack cellItem;
    protected final ISaveProvider container;
    protected long storedFluidCount;
    protected long storedFluids;
    protected IItemList<IAEFluidStack> cellFluids = null;
    protected final NBTTagCompound data;

    public InfinityFluidCellInventory(ItemStack o, ISaveProvider container, EntityPlayer player)
        throws AppEngException {
        if (o == null) {
            throw new AppEngException("ItemStack was used as a cell, but was not a cell!");
        }
        this.cellItem = o;
        this.cellType = (IStorageFluidCell) this.cellItem.getItem();
        this.container = container;
        this.data = Platform.openNbtData(o);
        this.storedFluids = this.data.getLong(FLUID_TYPE_TAG);
        this.storedFluidCount = this.data.getLong(FLUID_COUNT_TAG);
    }

    public static IMEInventoryHandler<IAEFluidStack> getCell(ItemStack o, ISaveProvider container,
        EntityPlayer player) {
        try {
            if (o.getItem() instanceof ItemInfinityFluidCell) {
                return new FluidCellInventoryHandler(new InfinityFluidCellInventory(o, container, player));
            }
        } catch (final AppEngException ignored) {

        }
        return null;
    }

    @Override
    public IAEFluidStack injectItems(IAEFluidStack input, Actionable mode, BaseActionSource src) {
        if (input == null) {
            return null;
        }
        if (input.getStackSize() == 0) {
            return null;
        }
        if (this.cellType.isBlackListed(this.cellItem, input)) {
            return input;
        }
        final IAEFluidStack l = this.getCellFluids()
            .findPrecise(input);

        if (l != null) {
            if (mode == Actionable.MODULATE) {
                l.setStackSize(l.getStackSize() + input.getStackSize());
                this.saveChanges();
            }
            return null;
        }

        if (this.canHoldNewFluid()) // room for new type, and for at least one item!
        {
            if (mode == Actionable.MODULATE) {
                this.cellFluids.add(input);
                this.saveChanges();
            }
            return null;
        }

        return input;
    }

    @Override
    public IAEFluidStack extractItems(IAEFluidStack request, Actionable mode, BaseActionSource src) {
        if (request == null) {
            return null;
        }

        final long size = request.getStackSize();

        IAEFluidStack results = null;

        final IAEFluidStack l = this.getCellFluids()
            .findPrecise(request);

        if (l != null) {
            results = l.copy();

            if (l.getStackSize() <= size) {
                results.setStackSize(l.getStackSize());

                if (mode == Actionable.MODULATE) {
                    l.setStackSize(0);
                    this.saveChanges();
                }
            } else {
                results.setStackSize(size);

                if (mode == Actionable.MODULATE) {
                    l.setStackSize(l.getStackSize() - size);
                    this.saveChanges();
                }
            }
        }

        return results;
    }

    private void saveChanges() {
        long size = 0;
        int count = 0;
        IItemList<IAEFluidStack> fluidList = this.getStorage()
            .getFluids();
        fluidList.resetStatus();
        for (final IAEFluidStack ias : this.getCellFluids()) {
            if (ias.getStackSize() > 0) {
                fluidList.add(ias);
                count++;
                size += ias.getStackSize();
            }
        }
        this.storedFluids = count;
        this.storedFluidCount = size;
        data.setLong(FLUID_TYPE_TAG, this.storedFluids);
        data.setLong(FLUID_COUNT_TAG, this.storedFluidCount);
        AE2ThingAPI.instance()
            .getStorageManager()
            .setDirty(true);
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
        DataStorage storage = this.getStorage();
        if (this.cellFluids == null) {
            this.cellFluids = AEApi.instance()
                .storage()
                .createFluidList();
        }
        this.cellFluids.resetStatus();
        for (final IAEFluidStack i : storage.getFluids()) {
            final IAEFluidStack ias = i.copy();
            if (ias.getStackSize() > 0) {
                this.cellFluids.add(ias);
            }
        }
        if (!this.getUUID()
            .equals(storage.getUUID())) {
            data.setString(Constants.DISKUUID, storage.getUUID());
        }
    }

    @Override
    public String getUUID() {
        if (data.hasNoTags()) {
            return "";
        }
        return data.getString(Constants.DISKUUID);
    }

    @Override
    public StorageChannel getChannel() {
        return StorageChannel.FLUIDS;
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
        return true;
    }

    @Override
    public long getTotalBytes() {
        return this.cellType.getBytes(this.cellItem);
    }

    @Override
    public long getFreeBytes() {
        return Long.MAX_VALUE;
    }

    @Override
    public long getUsedBytes() {
        return 0;
    }

    @Override
    public long getStoredFluidCount() {
        return this.storedFluidCount;
    }

    @Override
    public long getRemainingFluidCount() {
        return Long.MAX_VALUE;
    }

    @Override
    public long getRemainingFluidTypes() {
        return Long.MAX_VALUE;
    }

    @Override
    public int getUnusedFluidCount() {
        return Integer.MAX_VALUE;
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
        return this.storedFluids;
    }

    @Override
    public long getTotalFluidTypes() {
        return this.cellType.getTotalTypes(this.cellItem);
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

}
