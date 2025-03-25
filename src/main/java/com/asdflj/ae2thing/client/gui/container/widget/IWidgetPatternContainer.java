package com.asdflj.ae2thing.client.gui.container.widget;

import com.asdflj.ae2thing.client.gui.container.IPatternContainer;
import com.asdflj.ae2thing.client.gui.container.IPatternValueContainer;

import appeng.container.slot.AppEngSlot;

public interface IWidgetPatternContainer extends IPatternValueContainer {

    IPatternContainer getContainer();

    default void addMESlotToContainer(AppEngSlot s) {}
}
