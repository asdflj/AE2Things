package com.asdflj.ae2thing.api.adapter.findit;

import java.util.ArrayList;
import java.util.List;

import com.asdflj.ae2thing.util.StorageProvider;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.DimensionalCoord;
import thaumicenergistics.common.parts.PartEssentiaStorageBus;

public class EssentiaStorageBusAdapter implements IFindItAdapter {

    @Override
    public Class<? extends IGridHost> getCls() {
        return PartEssentiaStorageBus.class;
    }

    @Override
    public boolean supportFluid() {
        return true;
    }

    @Override
    public List<StorageProvider> getStorageProver(IGrid grid, IGridNode node, IAEItemStack item, boolean isFluid) {
        List<StorageProvider> list = new ArrayList<>();
        if (node.getMachine() instanceof PartEssentiaStorageBus bus) {
            List<IMEInventoryHandler> handlers = bus.getCellArray(StorageChannel.FLUIDS);
            for (IMEInventoryHandler handler : handlers) {
                if (findStack(handler, isFluid, item)) {
                    list.add(
                        new StorageProvider(
                            new DimensionalCoord(
                                bus.getHost()
                                    .getTile()),
                            bus.getSide()));
                }
            }

        }
        return list;
    }
}
