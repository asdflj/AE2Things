package com.asdflj.ae2thing.client.gui.widget;

import java.util.List;

import com.asdflj.ae2thing.client.me.AdvItemRepo;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.widgets.ISortSource;

public interface IGuiMonitor extends ISortSource {

    void postFluidUpdate(List<IAEFluidStack> list);

    void postUpdate(List<IAEItemStack> list);

    void setScrollBar();

    AdvItemRepo getRepo();

    void handleKeyboardInput();
}
