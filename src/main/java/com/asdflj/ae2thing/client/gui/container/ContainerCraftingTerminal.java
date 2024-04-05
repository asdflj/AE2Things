package com.asdflj.ae2thing.client.gui.container;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.inventory.item.PortableItemInventory;
import com.asdflj.ae2thing.network.SPacketMEItemInvUpdate;
import com.asdflj.ae2thing.util.Util;

import appeng.api.AEApi;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.TypeFilter;
import appeng.api.config.ViewItems;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerNull;
import appeng.container.slot.SlotCraftingMatrix;
import appeng.container.slot.SlotCraftingTerm;
import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.helpers.IContainerCraftingPacket;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;

public class ContainerCraftingTerminal extends AEBaseContainer implements IConfigurableObject, IConfigManagerHost,
    IMEMonitorHandlerReceiver<IAEItemStack>, IAEAppEngInventory, IContainerCraftingPacket {

    private final PortableItemInventory it;
    protected IConfigManagerHost gui;
    protected IConfigManager serverCM;
    protected final IConfigManager clientCM;
    protected ITerminalHost host;
    protected IMEMonitor<IAEItemStack> monitor;
    private final IItemList<IAEItemStack> items = AEApi.instance()
        .storage()
        .createItemList();
    private final SlotCraftingMatrix[] craftingSlots = new SlotCraftingMatrix[9];
    private final SlotCraftingTerm outputSlot;

    public ContainerCraftingTerminal(InventoryPlayer ip, ITerminalHost monitorable) {
        super(ip, monitorable);
        this.host = monitorable;
        this.clientCM = new ConfigManager(this);
        this.clientCM.registerSetting(Settings.SORT_BY, SortOrder.NAME);
        this.clientCM.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
        this.clientCM.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);
        this.clientCM.registerSetting(Settings.TYPE_FILTER, TypeFilter.ALL);
        if (Platform.isServer()) {
            this.serverCM = monitorable.getConfigManager();
            this.monitor = monitorable.getItemInventory();
            this.monitor.addListener(this, null);
            this.setCellInventory(this.monitor);
            this.setPowerSource((IEnergySource) monitorable);
        } else {
            this.monitor = null;
        }
        this.it = (PortableItemInventory) monitorable;
        this.lockSlot();
        final IInventory crafting = this.it.getInventoryByName("crafting");
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                this.addSlotToContainer(
                    this.craftingSlots[x
                        + y * 3] = new SlotCraftingMatrix(this, crafting, x + y * 3, 37 + x * 18, -72 + y * 18));
            }
        }
        AppEngInternalInventory output = new AppEngInternalInventory(this, 1);
        this.addSlotToContainer(
            this.outputSlot = new SlotCraftingTerm(
                this.getPlayerInv().player,
                this.getActionSource(),
                this.getPowerSource(),
                monitorable,
                crafting,
                crafting,
                output,
                131,
                -72 + 18,
                this));
        this.bindPlayerInventory(ip, 0, 0);
        this.onCraftMatrixChanged(crafting);
    }

    @Override
    public void removeCraftingFromCrafters(final ICrafting c) {
        super.removeCraftingFromCrafters(c);
        if (this.crafters.isEmpty() && this.monitor != null) {
            this.monitor.removeListener(this);
        }
    }

    private void lockSlot() {
        this.lockPlayerInventorySlot(this.it.getInventorySlot());
        for (int s : Util.getBackpackSlot(this.getInventoryPlayer().player)) {
            this.lockPlayerInventorySlot(s);
        }

    }

    @Override
    public void onCraftMatrixChanged(final IInventory par1IInventory) {
        final ContainerNull cn = new ContainerNull();
        final InventoryCrafting ic = new InventoryCrafting(cn, 3, 3);
        for (int x = 0; x < 9; x++) {
            ic.setInventorySlotContents(x, this.craftingSlots[x].getStack());
        }
        this.outputSlot.putStack(
            CraftingManager.getInstance()
                .findMatchingRecipe(ic, this.getPlayerInv().player.worldObj));
    }

    public void setGui(@Nonnull final IConfigManagerHost gui) {
        this.gui = gui;
    }

    public IMEMonitor<IAEItemStack> getMonitor() {
        return this.monitor;
    }

    protected boolean isInvalid() {
        return this.monitor == null;
    }

    @Override
    public void detectAndSendChanges() {
        if (Platform.isServer()) {
            if (isInvalid()) {
                this.setValidContainer(false);
            }
            if (this.serverCM != null) {
                for (final Settings set : this.serverCM.getSettings()) {
                    final Enum<?> sideLocal = this.serverCM.getSetting(set);
                    final Enum<?> sideRemote = this.clientCM.getSetting(set);

                    if (sideLocal != sideRemote) {
                        this.clientCM.putSetting(set, sideLocal);
                        for (final Object crafter : this.crafters) {
                            try {
                                NetworkHandler.instance.sendTo(
                                    new PacketValueConfig(set.name(), sideLocal.name()),
                                    (EntityPlayerMP) crafter);
                            } catch (final IOException e) {
                                AELog.debug(e);
                            }
                        }
                    }
                }
            }
            processItemList();
            super.detectAndSendChanges();
        }
    }

    @Override
    public void addCraftingToCrafters(final ICrafting c) {
        super.addCraftingToCrafters(c);
        this.queueInventory(c);
    }

    protected void processItemList() {
        if (!this.items.isEmpty()) {
            final IItemList<IAEItemStack> monitorCache = this.monitor.getStorageList();
            List<IAEItemStack> toSend = new ArrayList<>();
            for (final IAEItemStack is : this.items) {
                final IAEItemStack send = monitorCache.findPrecise(is);
                if (send != null) {
                    toSend.add(send);
                } else {
                    is.setStackSize(0);
                    toSend.add(is);
                }
            }
            SPacketMEItemInvUpdate piu = new SPacketMEItemInvUpdate();
            piu.addAll(toSend);
            for (final Object c : this.crafters) {
                if (c instanceof EntityPlayer) {
                    AE2Thing.proxy.netHandler.sendTo(piu, (EntityPlayerMP) c);
                }
            }
            this.items.resetStatus();
        }
    }

    @Override
    public IConfigManager getConfigManager() {
        if (Platform.isServer()) {
            return this.serverCM;
        }
        return this.clientCM;
    }

    @Override
    public boolean isValid(Object verificationToken) {
        return this.monitor != null;
    }

    @Override
    public void postChange(IBaseMonitor<IAEItemStack> monitor, Iterable<IAEItemStack> change,
        BaseActionSource actionSource) {
        for (final IAEItemStack is : change) {
            this.items.add(is);
        }
    }

    protected void queueInventory(final ICrafting c) {
        if (Platform.isServer() && c instanceof EntityPlayer && this.monitor != null) {
            final IItemList<IAEItemStack> monitorCache = this.monitor.getStorageList();
            List<IAEItemStack> toSend = new ArrayList<>();
            for (final IAEItemStack is : monitorCache) {
                toSend.add(is);
            }
            SPacketMEItemInvUpdate piu = new SPacketMEItemInvUpdate();
            piu.addAll(toSend);
            AE2Thing.proxy.netHandler.sendTo(piu, (EntityPlayerMP) c);
        }
    }

    @Override
    public void onListUpdate() {
        for (final Object c : this.crafters) {
            if (c instanceof final ICrafting cr) {
                this.queueInventory(cr);
            }
        }
    }

    protected IConfigManagerHost getGui() {
        return this.gui;
    }

    @Override
    public void updateSetting(IConfigManager manager, Enum settingName, Enum newValue) {
        if (this.getGui() != null) {
            this.getGui()
                .updateSetting(manager, settingName, newValue);
        }
    }

    @Override
    public IGridNode getNetworkNode() {
        return null;
    }

    @Override
    public IInventory getInventoryByName(String name) {
        if (name.equals("player")) {
            return this.getInventoryPlayer();
        }
        return this.it.getInventoryByName(name);
    }

    @Override
    public boolean useRealItems() {
        return true;
    }

    @Override
    public ItemStack[] getViewCells() {
        return new ItemStack[0];
    }

    @Override
    public void saveChanges() {

    }

    @Override
    public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removedStack,
        ItemStack newStack) {

    }
}
