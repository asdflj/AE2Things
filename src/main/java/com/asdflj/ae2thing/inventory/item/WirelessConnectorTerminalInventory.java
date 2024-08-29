package com.asdflj.ae2thing.inventory.item;

import com.asdflj.ae2thing.api.WirelessObject;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.util.IConfigManager;

public class WirelessConnectorTerminalInventory extends WirelessTerminal implements INetworkTerminal {

    public WirelessConnectorTerminalInventory(WirelessObject obj) {
        super(obj);
    }

    @Override
    public IConfigManager getConfigManager() {
        return null;
    }

    @Override
    public IGrid getGrid() {
        return obj.getGrid();
    }

    @Override
    public IGridNode getGridNode() {
        return obj.getGridNode();
    }

}
