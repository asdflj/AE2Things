package com.asdflj.ae2thing.inventory.item;

import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.api.WirelessObject;
import com.asdflj.ae2thing.inventory.WirelessConnectorTerminal;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.IConfigManager;

public class WirelessConnectorTerminalInventory extends WirelessTerminal implements WirelessConnectorTerminal {

    public WirelessConnectorTerminalInventory(WirelessObject obj) {
        super(obj);
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
        return obj.getItemInventory();
    }

    @Override
    public IMEMonitor<IAEFluidStack> getFluidInventory() {
        return this.obj.getFluidInventory();
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

    @Override
    public IGridNode getGridNode() {
        return obj.getGridNode();
    }

}
