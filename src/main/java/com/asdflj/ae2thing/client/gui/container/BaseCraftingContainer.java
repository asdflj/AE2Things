package com.asdflj.ae2thing.client.gui.container;

import net.minecraft.entity.player.InventoryPlayer;

import appeng.api.storage.ITerminalHost;
import appeng.container.AEBaseContainer;

public class BaseCraftingContainer extends AEBaseContainer {

    public BaseCraftingContainer(InventoryPlayer ip, ITerminalHost host) {
        super(ip, host);
    }
}
