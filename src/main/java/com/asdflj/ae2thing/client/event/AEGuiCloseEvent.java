package com.asdflj.ae2thing.client.event;

import appeng.client.gui.AEBaseGui;
import cpw.mods.fml.common.eventhandler.Event;

public class AEGuiCloseEvent extends Event {

    private final AEBaseGui gui;

    public AEGuiCloseEvent(AEBaseGui gui) {
        this.gui = gui;
    }

    public AEBaseGui getGui() {
        return gui;
    }
}
