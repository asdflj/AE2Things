package com.asdflj.ae2thing.inventory;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;

public interface WirelessConnectorTerminal {

    IGrid getGrid();

    IGridNode getGridNode();
}
