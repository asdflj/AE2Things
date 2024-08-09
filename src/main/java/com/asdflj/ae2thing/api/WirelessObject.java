package com.asdflj.ae2thing.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.asdflj.ae2thing.common.Config;
import com.asdflj.ae2thing.inventory.item.WirelessConnectorTerminalInventory;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.exceptions.AppEngException;
import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.util.WorldCoord;
import appeng.core.localization.PlayerMessages;
import appeng.items.tools.powered.ToolWirelessTerminal;
import appeng.tile.networking.TileWireless;
import appeng.util.Platform;

public class WirelessObject {

    private final ItemStack item;
    private final World world;
    private final int x;
    private final int y;
    private final int z;
    private final EntityPlayer player;
    private IGridNode gridNode;
    private IGrid grid;
    private IEnergySource energySource;
    private WirelessConnectorTerminalInventory wirelessConnectorTerminal;

    public WirelessObject(ItemStack item, World world, int x, int y, int z, EntityPlayer player)
        throws AppEngException {
        this.item = item;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.player = player;
        if (Platform.isServer()) {
            this.gridNode = getWirelessGrid();
            if (gridNode == null || !rangeCheck()) {
                throw new AppEngException(
                    PlayerMessages.OutOfRange.get()
                        .toString());
            }
            this.grid = this.gridNode.getGrid();
        }
    }

    public ItemStack getItemStack() {
        return item;
    }

    public IGridNode getGridNode() {
        return gridNode;
    }

    public int getSlot() {
        return x;
    }

    public IGrid getGrid() {
        return grid;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    public void setEnergySource(IEnergySource energySource) {
        this.energySource = energySource;
    }

    public boolean rangeCheck() {
        if (Config.wirelessConnectorTerminalInfinityConnectionRange) return true;
        boolean canConnect = false;
        for (IGridNode node : this.gridNode.getGrid()
            .getMachines(TileWireless.class)) {
            IWirelessAccessPoint accessPoint = (IWirelessAccessPoint) node.getMachine();
            if (accessPoint.isActive() && accessPoint.getLocation()
                .getDimension() == player.dimension) {
                WorldCoord distance = accessPoint.getLocation()
                    .subtract((int) player.posX, (int) player.posY, (int) player.posZ);
                int squaredDistance = distance.x * distance.x + distance.y * distance.y + distance.z * distance.z;
                if (squaredDistance <= accessPoint.getRange() * accessPoint.getRange()) {
                    canConnect = true;
                    break;
                }
            }
        }
        return canConnect;
    }

    public Object getInventory() {
        if (wirelessConnectorTerminal == null) {
            this.wirelessConnectorTerminal = new WirelessConnectorTerminalInventory(this);
        }
        return wirelessConnectorTerminal;
    }

    public IGridNode getWirelessGrid() {
        if (item.getItem() instanceof ToolWirelessTerminal) {
            String key = ((ToolWirelessTerminal) item.getItem()).getEncryptionKey(item);
            IGridHost securityTerminal = (IGridHost) AEApi.instance()
                .registries()
                .locatable()
                .getLocatableBy(Long.parseLong(key));
            if (securityTerminal == null) return null;
            return securityTerminal.getGridNode(ForgeDirection.UNKNOWN);
        }
        return null;
    }

    private void closeGui() {
        this.player.addChatMessage(PlayerMessages.DeviceNotPowered.get());
        this.player.closeScreen();
    }

    public int extractPower(double amt, Actionable mode, PowerMultiplier usePowerMultiplier, int ticks) {
        if (mode == Actionable.SIMULATE) return ticks;
        if (ticks < 10) {
            ticks++;
            return ticks;
        }
        if (this.energySource == null) closeGui();
        double result = energySource.extractAEPower(amt, mode, usePowerMultiplier);
        if (result == 0) closeGui();
        this.player.inventory.setInventorySlotContents(getSlot(), getItemStack());
        ticks = 0;
        return ticks;
    }
}
