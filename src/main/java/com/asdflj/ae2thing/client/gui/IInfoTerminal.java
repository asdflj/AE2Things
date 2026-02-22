package com.asdflj.ae2thing.client.gui;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;

import com.asdflj.ae2thing.client.gui.widget.IClickable;
import com.asdflj.ae2thing.client.gui.widget.IScrollable;

import appeng.client.gui.AEBaseGui;

public interface IInfoTerminal {

    List<IClickable> getClickables();

    List<IScrollable> getScrollables();

    AEBaseGui getGui();

    void postUpdate(NBTTagCompound data);

    default String getBackground() {
        return "";
    };
}
