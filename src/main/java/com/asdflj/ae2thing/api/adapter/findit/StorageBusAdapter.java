package com.asdflj.ae2thing.api.adapter.findit;

import java.util.List;

import com.asdflj.ae2thing.util.StorageProvider;
import com.google.common.collect.ImmutableList;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.DimensionalCoord;
import appeng.parts.misc.PartStorageBus;

public class StorageBusAdapter implements IFindItAdapter {

    @Override
    public Class<? extends IGridHost> getCls() {
        return PartStorageBus.class;
    }

    @Override
    public boolean supportFluid() {
        return false;
    }

    @Override
    public List<StorageProvider> getStorageProver(IGrid grid, IGridNode node, IAEItemStack item, boolean isFluid) {
        if (node.getMachine() instanceof PartStorageBus bus) {
            if (findStack(bus.getInternalHandler(), isFluid, item)) {
                return ImmutableList.of(new StorageProvider(new DimensionalCoord(bus.getTile()), bus.getSide()));
            }
        }
        return null;
    }
}
