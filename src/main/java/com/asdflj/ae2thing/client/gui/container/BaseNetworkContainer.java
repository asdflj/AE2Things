package com.asdflj.ae2thing.client.gui.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.client.gui.container.widget.IWidgetPatternContainer;
import com.asdflj.ae2thing.inventory.item.INetworkTerminal;
import com.asdflj.ae2thing.inventory.item.WirelessTerminal;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.storage.ITerminalHost;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.items.misc.ItemEncodedPattern;
import appeng.me.helpers.ChannelPowerSrc;
import appeng.util.Platform;

public class BaseNetworkContainer extends AEBaseContainer {

    protected final EntityPlayer player;
    protected WirelessTerminal terminal;
    protected int ticks;
    protected final double powerMultiplier = 0.5;

    @GuiSync(1)
    public boolean hasPower = false;

    public BaseNetworkContainer(InventoryPlayer ip, ITerminalHost host) {
        super(ip, host);
        this.player = ip.player;
        if (Platform.isClient()) return;
        if (host instanceof WirelessTerminal) {
            this.terminal = (WirelessTerminal) host;
            this.setPowerSource(this.terminal);
        } else if (this instanceof INetworkTerminal it) {
            this.setPowerSource(
                new ChannelPowerSrc(
                    it.getGridNode(),
                    it.getGrid()
                        .getCache(IEnergyGrid.class)));
        }
    }

    private boolean transferPatternToSlot(EntityPlayer p, int idx, IPatternContainer container) {
        Slot clickSlot = this.inventorySlots.get(idx);
        ItemStack is = clickSlot.getStack();
        if (is != null && !container.getPatternOutputSlot()
            .getHasStack() && is.stackSize == 1 && is.getItem() instanceof ItemEncodedPattern) {
            ItemStack output = is.copy();
            container.getPatternOutputSlot()
                .putStack(output);
            p.inventory.setInventorySlotContents(clickSlot.getSlotIndex(), null);
            return true;
        }
        return false;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer p, int idx) {
        boolean didSomething = false;
        if (this instanceof IPatternContainer patternContainer) {
            didSomething = transferPatternToSlot(p, idx, patternContainer);
        } else if (this instanceof IWidgetPatternContainer w) {
            didSomething = transferPatternToSlot(p, idx, w.getContainer());
        }
        if (didSomething) {
            return null;
        }
        return super.transferStackInSlot(p, idx);
    }

    public BaseNetworkContainer(InventoryPlayer ip, Object anchor) {
        super(ip, anchor);
        this.player = ip.player;
    }

    protected void updatePowerStatus() {
        try {
            if (this.getNetworkNode() != null) {
                this.setPowered(
                    this.getNetworkNode()
                        .isActive());
            } else if (this.getPowerSource() instanceof IEnergyGrid) {
                this.setPowered(((IEnergyGrid) this.getPowerSource()).isNetworkPowered());
            } else {
                this.setPowered(
                    this.getPowerSource()
                        .extractAEPower(1, Actionable.SIMULATE, PowerMultiplier.CONFIG) > 0.8);
            }
        } catch (final Throwable ignore) {}
    }

    private IGridNode getNetworkNode() {
        if (this.terminal == null) return null;
        return this.terminal.getGridNode();
    }

    @Override
    public void addCraftingToCrafters(ICrafting crafting) {
        updatePowerStatus();
        super.addCraftingToCrafters(crafting);
    }

    protected void setPowered(final boolean isPowered) {
        this.hasPower = isPowered;
    }

    @Override
    public void detectAndSendChanges() {
        if (Platform.isServer()) {
            if (this.terminal != null && this.hasPower) {
                ticks = this.terminal.getWirelessObject()
                    .extractPower(getPowerMultiplier() * ticks, Actionable.MODULATE, PowerMultiplier.CONFIG, ticks);
            }
        }
        super.detectAndSendChanges();
    }

    public double getPowerMultiplier() {
        return this.powerMultiplier;
    }
}
