package com.asdflj.ae2thing.api.adapter.findit;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.asdflj.ae2thing.util.StorageProvider;
import com.glodblock.github.util.Util;

import appeng.api.AEApi;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEFluidStack;
import codechicken.nei.recipe.StackInfo;

public interface IFindItAdapter {

    Class<? extends appeng.api.networking.IGridHost> getCls();

    boolean supportFluid();

    default boolean isFluid(ItemStack item) {
        return this.supportFluid() && StackInfo.getFluid(item) != null;
    }

    default List<StorageProvider> getStorageProviders(IGrid grid, IAEItemStack item) {
        boolean isFluid = this.isFluid(item.getItemStack());
        List<StorageProvider> list = new ArrayList<>();
        for (IGridNode node : grid.getMachines(getCls())) {
            List<StorageProvider> results = getStorageProver(grid, node, item, isFluid);
            if (results != null) {
                list.addAll(results);
            }
        }
        return list;
    }

    List<StorageProvider> getStorageProver(IGrid grid, IGridNode node, IAEItemStack item, boolean isFluid);

    default boolean findStack(IMEInventory inv, boolean isFluid, IAEItemStack request) {
        if (inv == null) return false;
        boolean result = false;
        if (isFluid && inv.getChannel() == StorageChannel.FLUIDS) {
            FluidStack fs = Util.getFluidFromItem(request.getItemStack());
            result = inv.getAvailableItem(AEFluidStack.create(fs)) != null;
        } else if (inv.getChannel() == StorageChannel.ITEMS) {
            result = inv.getAvailableItem(request) != null;
        }
        return result;
    }

    default boolean findStack(ItemStack cell, boolean isFluid, IAEItemStack request) {
        IMEInventory inv = getInv(cell, isFluid ? StorageChannel.FLUIDS : StorageChannel.ITEMS);
        return findStack(inv, isFluid, request);
    }

    default IMEInventory getInv(final ItemStack is, StorageChannel channel) {
        if (channel == StorageChannel.ITEMS) {
            return AEApi.instance()
                .registries()
                .cell()
                .getCellInventory(is, null, StorageChannel.ITEMS);
        } else if (channel == StorageChannel.FLUIDS) {
            return AEApi.instance()
                .registries()
                .cell()
                .getCellInventory(is, null, StorageChannel.FLUIDS);
        }
        return null;
    }
}
