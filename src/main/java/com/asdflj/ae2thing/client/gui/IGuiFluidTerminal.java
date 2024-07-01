package com.asdflj.ae2thing.client.gui;

import java.util.List;

import appeng.api.storage.data.IAEFluidStack;

public interface IGuiFluidTerminal {

    void postFluidUpdate(List<IAEFluidStack> list);
}
