package com.asdflj.ae2thing.inventory.item;

import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.api.WirelessObject;
import com.asdflj.ae2thing.inventory.WirelessConnectorTerminal;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.networking.IGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.IConfigManager;
import appeng.container.interfaces.IInventorySlotAware;
import appeng.items.tools.powered.ToolWirelessTerminal;

public class WirelessConnectorTerminalInventory
    implements ITerminalHost, IInventorySlotAware, IGuiItemObject, IEnergySource, WirelessConnectorTerminal {

    private final WirelessObject obj;
    private final IAEItemPowerStorage ips;

    public WirelessConnectorTerminalInventory(WirelessObject obj) {
        this.obj = obj;
        this.ips = (ToolWirelessTerminal) obj.getItemStack()
            .getItem();
        this.obj.setEnergySource(this);
    }

    public WirelessObject getWirelessObject() {
        return obj;
    }

    @Override
    public ItemStack getItemStack() {
        return obj.getItemStack();
    }

    @Override
    public double extractAEPower(double amt, Actionable mode, PowerMultiplier usePowerMultiplier) {
        amt = usePowerMultiplier.multiply(amt);
        if (mode == Actionable.SIMULATE) {
            return usePowerMultiplier.divide(Math.min(amt, this.ips.getAECurrentPower(this.getItemStack())));
        }
        return usePowerMultiplier.divide(this.ips.extractAEPower(this.getItemStack(), amt));

    }

    @Override
    public IMEMonitor<IAEItemStack> getItemInventory() {
        return null;
    }

    @Override
    public IMEMonitor<IAEFluidStack> getFluidInventory() {
        return null;
    }

    @Override
    public IConfigManager getConfigManager() {
        return null;
    }

    @Override
    public int getInventorySlot() {
        return obj.getSlot();
    }

    @Override
    public IGrid getGrid() {
        return obj.getGrid();
    }

}
