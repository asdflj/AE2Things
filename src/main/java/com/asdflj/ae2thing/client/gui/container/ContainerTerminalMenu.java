package com.asdflj.ae2thing.client.gui.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class ContainerTerminalMenu extends Container {

    public ContainerTerminalMenu() {
        super();
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
