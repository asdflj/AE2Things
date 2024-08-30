package com.asdflj.ae2thing.client.gui;

import java.util.List;

import net.minecraft.client.gui.GuiButton;

import com.asdflj.ae2thing.client.gui.widget.IAEBasePanel;

import appeng.client.gui.AEBaseGui;

public interface IWidgetGui {

    void setXSize(int size);

    AEBaseGui getGui();

    boolean hideItemPanelSlot(int x, int y, int w, int h);

    List<GuiButton> getButtonList();

    IAEBasePanel getPanel();

}
