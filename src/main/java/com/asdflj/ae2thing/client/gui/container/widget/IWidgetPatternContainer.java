package com.asdflj.ae2thing.client.gui.container.widget;

import com.asdflj.ae2thing.client.gui.container.IPatternContainer;

import appeng.container.slot.AppEngSlot;

public interface IWidgetPatternContainer {

    IPatternContainer getContainer();

    default void addMESlotToContainer(AppEngSlot s) {}
}
