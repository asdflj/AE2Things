package com.asdflj.ae2thing.loader;

import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.api.adapter.pattern.AEPatternTerminal;
import com.asdflj.ae2thing.api.adapter.pattern.AEPatternTerminalEx;
import com.asdflj.ae2thing.api.adapter.pattern.AEPatternTerminalExTransferHandler;
import com.asdflj.ae2thing.api.adapter.pattern.AEPatternTerminalTransferHandler;
import com.asdflj.ae2thing.api.adapter.pattern.FCPatternTerminal;
import com.asdflj.ae2thing.api.adapter.pattern.FCPatternTerminalTransferHandler;
import com.glodblock.github.client.gui.container.ContainerFluidPatternExWireless;
import com.glodblock.github.client.gui.container.ContainerFluidPatternTerminal;
import com.glodblock.github.client.gui.container.ContainerFluidPatternTerminalEx;
import com.glodblock.github.client.gui.container.ContainerFluidPatternWireless;

public class BRLoader implements Runnable {

    @Override
    public void run() {
        AE2ThingAPI.instance()
            .terminal()
            .registerPatternTerminal(new AEPatternTerminal())
            .registerIdentifier(Constants.NEI_BR, new AEPatternTerminalTransferHandler());
        AE2ThingAPI.instance()
            .terminal()
            .registerPatternTerminal(new AEPatternTerminalEx())
            .registerIdentifier(Constants.NEI_BR, new AEPatternTerminalExTransferHandler());
        FCPatternTerminalTransferHandler handler = new FCPatternTerminalTransferHandler();
        AE2ThingAPI.instance()
            .terminal()
            .registerPatternTerminal(
                new FCPatternTerminal(ContainerFluidPatternTerminal.class)
                    .registerIdentifier(Constants.NEI_BR, handler));
        AE2ThingAPI.instance()
            .terminal()
            .registerPatternTerminal(
                new FCPatternTerminal(ContainerFluidPatternWireless.class)
                    .registerIdentifier(Constants.NEI_BR, handler));
        AE2ThingAPI.instance()
            .terminal()
            .registerPatternTerminal(
                new FCPatternTerminal(ContainerFluidPatternTerminalEx.class)
                    .registerIdentifier(Constants.NEI_BR, handler));
        AE2ThingAPI.instance()
            .terminal()
            .registerPatternTerminal(
                new FCPatternTerminal(ContainerFluidPatternExWireless.class)
                    .registerIdentifier(Constants.NEI_BR, handler));

    }
}
