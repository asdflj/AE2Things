package com.asdflj.ae2thing.inventory.item;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;

public interface INetworkTerminal {

    IGrid getGrid();

    IGridNode getGridNode();
}
