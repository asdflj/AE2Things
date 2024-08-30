package com.asdflj.ae2thing.inventory.item;

import com.asdflj.ae2thing.util.Util;

public interface IClickableInTerminal {

    void setClickedInterface(Util.DimensionalCoordSide tile);

    Util.DimensionalCoordSide getClickedInterface();
}
