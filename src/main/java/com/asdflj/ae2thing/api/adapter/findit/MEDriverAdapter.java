package com.asdflj.ae2thing.api.adapter.findit;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.util.StorageProvider;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.DimensionalCoord;
import appeng.tile.storage.TileDrive;

public class MEDriverAdapter implements IFindItAdapter {

    @Override
    public Class<? extends IGridHost> getCls() {
        return TileDrive.class;
    }

    @Override
    public boolean supportFluid() {
        return true;
    }

    @Override
    public List<StorageProvider> getStorageProver(IGrid grid, IGridNode node, IAEItemStack item, boolean isFluid) {
        List<StorageProvider> list = new ArrayList<>();
        if (node.getMachine() instanceof TileDrive drive) {
            for (int i = 0; i < drive.getCellCount(); i++) {
                ItemStack is = drive.getStackInSlot(i);
                if (findStack(is, isFluid, item)) {
                    list.add(new StorageProvider(new DimensionalCoord(drive), i));
                }
            }
        }
        return list;
    }

}
