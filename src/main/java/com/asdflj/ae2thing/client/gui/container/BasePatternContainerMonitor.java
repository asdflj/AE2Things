package com.asdflj.ae2thing.client.gui.container;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraftforge.common.util.ForgeDirection;

import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.inventory.IPatternTerminal;
import com.asdflj.ae2thing.inventory.item.INetworkTerminal;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.ITerminalHost;
import appeng.api.util.IConfigurableObject;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.SlotRestrictedInput;
import appeng.helpers.IContainerCraftingPacket;
import appeng.me.helpers.ChannelPowerSrc;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.util.IConfigManagerHost;

public abstract class BasePatternContainerMonitor extends ContainerMonitor implements IConfigurableObject,
    IConfigManagerHost, IAEAppEngInventory, IContainerCraftingPacket, IPatternContainer {

    protected SlotRestrictedInput patternSlotIN;
    protected SlotRestrictedInput patternSlotOUT;
    protected SlotRestrictedInput patternRefiller;

    protected final IInventory crafting;
    protected final IInventory output;
    protected final IInventory patternInv;

    @GuiSync(99)
    public boolean canAccessViewCells;
    @GuiSync(95)
    public boolean combine = false;
    @GuiSync(92)
    public int activePage = 0;
    @GuiSync(100)
    public boolean craftingMode = true;
    protected final IPatternTerminal it;

    public BasePatternContainerMonitor(InventoryPlayer ip, ITerminalHost monitorable) {
        super(ip, monitorable);
        this.it = (IPatternTerminal) monitorable;
        this.canAccessViewCells = false;
        this.crafting = this.it.getInventoryByName(Constants.CRAFTING);
        this.output = this.it.getInventoryByName(Constants.OUTPUT);
        this.patternInv = this.it.getInventoryByName(Constants.PATTERN);
    }

    @Override
    void setMonitor() {
        if (this.host instanceof INetworkTerminal) {
            final IGridNode node = ((IGridHost) this.host).getGridNode(ForgeDirection.UNKNOWN);
            if (node != null) {
                this.networkNode = node;
                final IGrid g = node.getGrid();
                if (g != null) {
                    this.setPowerSource(new ChannelPowerSrc(this.networkNode, g.getCache(IEnergyGrid.class)));
                    IStorageGrid storageGrid = g.getCache(IStorageGrid.class);
                    this.monitor.setMonitor(storageGrid.getItemInventory());
                    this.fluidMonitor.setMonitor(storageGrid.getFluidInventory(), storageGrid.getItemInventory());
                    this.monitor.setFluidMonitorObject(this.fluidMonitor);
                    if (this.monitor.getMonitor() == null) {
                        this.setValidContainer(false);
                    } else {
                        this.monitor.addListener();
                        this.fluidMonitor.addListener();
                        this.setCellInventory(this.monitor.getMonitor());
                    }
                }
            } else {
                this.setValidContainer(false);
            }
        }
    }

    @Override
    public Slot getPatternOutputSlot() {
        return this.patternSlotOUT;
    }
}
