package com.asdflj.ae2thing.client.event;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.adapter.terminal.item.TerminalItems;
import com.asdflj.ae2thing.network.CPacketOpenTerminal;

import cpw.mods.fml.common.eventhandler.Event;

public class OpenTerminalEvent extends Event {

    private final TerminalItems items;

    public OpenTerminalEvent(TerminalItems terminalItems) {
        items = terminalItems;
    }

    public void openTerminal() {
        AE2Thing.proxy.netHandler.sendToServer(new CPacketOpenTerminal(items));
    }
}
