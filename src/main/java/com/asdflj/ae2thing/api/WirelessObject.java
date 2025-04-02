package com.asdflj.ae2thing.api;

import java.lang.reflect.InvocationTargetException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.asdflj.ae2thing.common.Config;
import com.asdflj.ae2thing.common.item.ItemWirelessConnectorTerminal;
import com.asdflj.ae2thing.common.item.ItemWirelessDualInterfaceTerminal;
import com.asdflj.ae2thing.inventory.item.WirelessTerminal;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.exceptions.AppEngException;
import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.PlayerSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.WorldCoord;
import appeng.core.localization.PlayerMessages;
import appeng.items.tools.powered.ToolWirelessTerminal;
import appeng.tile.networking.TileWireless;
import appeng.util.Platform;

public class WirelessObject implements IActionHost {

    private final ItemStack item;
    private final World world;
    private final int x;
    private final int y;
    private final int z;
    private final EntityPlayer player;
    private IGridNode gridNode;
    private IGrid grid;
    private IEnergySource energySource;
    private IMEMonitor<IAEItemStack> itemInv;
    private IMEMonitor<IAEFluidStack> fluidInv;
    private WirelessTerminal wirelessTerminal;
    private PlayerSource source;

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
            IStorageGrid iStorageGrid = this.getGrid()
                .getCache(IStorageGrid.class);
            this.itemInv = iStorageGrid.getItemInventory();
            this.fluidInv = iStorageGrid.getFluidInventory();
            this.source = new PlayerSource(this.player, this);
        }
    }

    public IMEMonitor<IAEItemStack> getItemInventory() {
        return this.itemInv;
    }

    public IMEMonitor<IAEFluidStack> getFluidInventory() {
        return this.fluidInv;
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
        if (this.getItemStack() != null && this.getItemStack()
            .getItem() != null) {
            Item i = this.getItemStack()
                .getItem();
            if (i instanceof ItemWirelessConnectorTerminal) {
                if (Config.wirelessConnectorTerminalInfinityConnectionRange) return true;
            } else if (i instanceof ItemWirelessDualInterfaceTerminal && this.hasInfinityBoosterCard()) {
                return true;
            }
        }
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

    public Object getInventory(Class<?> obj)
        throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (wirelessTerminal == null) {
            this.wirelessTerminal = (WirelessTerminal) obj.getConstructor(WirelessObject.class)
                .newInstance(this);
        }
        return wirelessTerminal;
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
        if (this.hasEnergyCard()) return 0;
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

    public static boolean hasInfinityBoosterCard(ItemStack item) {
        NBTTagCompound tag = Platform.openNbtData(item);
        return hasInfinityBoosterCard(tag);
    }

    private static boolean hasInfinityBoosterCard(NBTTagCompound data) {
        return data.hasKey(Constants.INFINITY_BOOSTER_CARD) && data.getBoolean(Constants.INFINITY_BOOSTER_CARD);
    }

    public boolean hasInfinityBoosterCard() {
        return hasInfinityBoosterCard(item);
    }

    public static boolean hasEnergyCard(ItemStack is) {
        NBTTagCompound data = Platform.openNbtData(is);
        return hasEnergyCard(data);
    }

    private static boolean hasEnergyCard(NBTTagCompound data) {
        return data.hasKey(Constants.INFINITY_ENERGY_CARD) && data.getBoolean(Constants.INFINITY_ENERGY_CARD);
    }

    public boolean hasEnergyCard() {
        return hasEnergyCard(item);
    }

    @Override
    public IGridNode getActionableNode() {
        return this.gridNode;
    }

    @Override
    public IGridNode getGridNode(ForgeDirection dir) {
        return this.gridNode;
    }

    @Override
    public AECableType getCableConnectionType(ForgeDirection dir) {
        return AECableType.NONE;
    }

    @Override
    public void securityBreak() {
        this.getGridNode(ForgeDirection.UNKNOWN)
            .getMachine()
            .securityBreak();
    }

    public PlayerSource getSource() {
        return this.source;
    }
}
