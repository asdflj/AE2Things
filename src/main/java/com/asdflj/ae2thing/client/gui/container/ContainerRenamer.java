package com.asdflj.ae2thing.client.gui.container;

import net.minecraft.entity.player.InventoryPlayer;

import appeng.api.storage.ITerminalHost;
import appeng.container.AEBaseContainer;

public class ContainerRenamer extends AEBaseContainer {

    public ContainerRenamer(InventoryPlayer ip, ITerminalHost monitorable) {
        super(ip, monitorable);
    }
}
