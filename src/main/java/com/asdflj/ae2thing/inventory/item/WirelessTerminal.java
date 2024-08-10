package com.asdflj.ae2thing.inventory.item;

import com.asdflj.ae2thing.api.WirelessObject;

import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.storage.ITerminalHost;
import appeng.container.interfaces.IInventorySlotAware;
import appeng.items.tools.powered.ToolWirelessTerminal;

public abstract class WirelessTerminal implements ITerminalHost, IInventorySlotAware, IGuiItemObject, IEnergySource {

    protected final WirelessObject obj;
    protected final IAEItemPowerStorage ips;

    public WirelessTerminal(WirelessObject obj) {
        this.obj = obj;
        this.ips = (ToolWirelessTerminal) obj.getItemStack()
            .getItem();
        this.obj.setEnergySource(this);
    }
}
