package com.asdflj.ae2thing.inventory.item;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import org.jetbrains.annotations.NotNull;

import com.asdflj.ae2thing.api.WirelessObject;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.AECableType;
import appeng.container.interfaces.IInventorySlotAware;
import appeng.items.tools.powered.ToolWirelessTerminal;

public abstract class WirelessTerminal
    implements ITerminalHost, IInventorySlotAware, IGuiItemObject, IEnergySource, INetworkTerminal, IActionHost {

    protected final WirelessObject obj;
    protected final IAEItemPowerStorage ips;

    public WirelessTerminal(WirelessObject obj) {
        this.obj = obj;
        this.ips = (ToolWirelessTerminal) obj.getItemStack()
            .getItem();
        this.obj.setEnergySource(this);
    }

    @Override
    public IGridNode getActionableNode() {
        return this.obj.getGridNode();
    }

    @Override
    public IGridNode getGridNode(ForgeDirection dir) {
        return this.obj.getGridNode();
    }

    @Override
    public AECableType getCableConnectionType(ForgeDirection dir) {
        return this.obj.getCableConnectionType(dir);
    }

    @Override
    public void securityBreak() {
        this.obj.securityBreak();
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

    public BaseActionSource getActionSource() {
        return this.obj.getSource();
    }

    @NotNull
    public IAEStack<?> injectStack(IAEStack<?> stack, Actionable mode) {
        if (stack instanceof IAEItemStack) {
            return this.getItemInventory()
                .injectItems((IAEItemStack) stack, mode, this.getActionSource());
        } else {
            return this.getFluidInventory()
                .injectItems((IAEFluidStack) stack, mode, this.getActionSource());
        }
    }

    @NotNull
    public IAEStack<?> extractStack(IAEStack<?> stack, Actionable mode) {
        if (stack instanceof IAEItemStack) {
            return this.getItemInventory()
                .extractItems((IAEItemStack) stack, mode, this.getActionSource());
        } else {
            return this.getFluidInventory()
                .extractItems((IAEFluidStack) stack, mode, this.getActionSource());
        }
    }
}
