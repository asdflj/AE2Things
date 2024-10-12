package com.asdflj.ae2thing.client.gui.container;

import net.minecraft.entity.player.InventoryPlayer;

import com.asdflj.ae2thing.common.parts.SharedManaBus;

import appeng.tile.inventory.AppEngInternalAEInventory;

public class ContainerManaIO extends ContainerManaConfigurable {

    private final SharedManaBus bus;

    public ContainerManaIO(InventoryPlayer ip, SharedManaBus te) {
        super(ip, te);
        this.bus = te;
    }

    @Override
    public AppEngInternalAEInventory getFakeFluidInv() {
        return (AppEngInternalAEInventory) this.bus.getInventoryByName("config");
    }
}
