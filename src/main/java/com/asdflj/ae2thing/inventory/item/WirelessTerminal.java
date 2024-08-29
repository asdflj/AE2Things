package com.asdflj.ae2thing.inventory.item;

import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.api.WirelessObject;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.interfaces.IInventorySlotAware;
import appeng.items.tools.powered.ToolWirelessTerminal;

public abstract class WirelessTerminal
    implements ITerminalHost, IInventorySlotAware, IGuiItemObject, IEnergySource, INetworkTerminal {

    protected final WirelessObject obj;
    protected final IAEItemPowerStorage ips;

    public WirelessTerminal(WirelessObject obj) {
        this.obj = obj;
        this.ips = (ToolWirelessTerminal) obj.getItemStack()
            .getItem();
        this.obj.setEnergySource(this);
    }

    @Override
    public IGrid getGrid() {
        return obj.getGrid();
    }

    @Override
    public IGridNode getGridNode() {
        return obj.getGridNode();
    }

    public WirelessObject getWirelessObject() {
        return obj;
    }

    @Override
    public ItemStack getItemStack() {
        return obj.getItemStack();
    }

    @Override
    public int getInventorySlot() {
        return obj.getSlot();
    }

    @Override
    public IMEMonitor<IAEItemStack> getItemInventory() {
        return obj.getItemInventory();
    }

    @Override
    public IMEMonitor<IAEFluidStack> getFluidInventory() {
        return this.obj.getFluidInventory();
    }

    @Override
    public double extractAEPower(double amt, Actionable mode, PowerMultiplier usePowerMultiplier) {
        amt = usePowerMultiplier.multiply(amt);
        if (mode == Actionable.SIMULATE) {
            return usePowerMultiplier.divide(Math.min(amt, this.ips.getAECurrentPower(this.getItemStack())));
        }
        return usePowerMultiplier.divide(this.ips.extractAEPower(this.getItemStack(), amt));

    }

}
