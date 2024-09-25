package com.asdflj.ae2thing.client.gui;

import java.util.List;

import net.minecraft.client.gui.GuiButton;

import com.asdflj.ae2thing.client.gui.widget.IAEBasePanel;

import appeng.client.gui.AEBaseGui;
import appeng.client.me.InternalSlotME;

public interface IWidgetGui {

    AEBaseGui getGui();

    boolean hideItemPanelSlot(int x, int y, int w, int h);

    List<GuiButton> getButtonList();

    IAEBasePanel getActivePanel();

    List<InternalSlotME> getMeSlots();
}
