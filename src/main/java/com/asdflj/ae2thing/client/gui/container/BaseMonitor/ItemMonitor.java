package com.asdflj.ae2thing.client.gui.container.BaseMonitor;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ICrafting;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.network.SPacketMEItemInvUpdate;
import com.glodblock.github.common.item.ItemFluidDrop;

import appeng.api.AEApi;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.Platform;
import thaumicenergistics.common.items.ItemCraftingAspect;

public class ItemMonitor implements IMEMonitorHandlerReceiver<IAEItemStack>, IProcessItemList {

    private IMEMonitor<IAEItemStack> itemMonitor;
    private final IItemList<IAEItemStack> items = AEApi.instance()
        .storage()
        .createItemList();
    private List<ICrafting> crafters = null;
    private FluidMonitor fluidMonitorObject = null;

    public ItemMonitor(List<ICrafting> crafters) {
        this.crafters = crafters;
    }

    public void setMonitor(IMEMonitor<IAEItemStack> itemMonitor) {
        this.itemMonitor = itemMonitor;
    }

    public void setFluidMonitorObject(FluidMonitor objectMonitor) {
        this.fluidMonitorObject = objectMonitor;
    }

    @Override
    public void addListener() {
        this.itemMonitor.addListener(this, null);
    }

    @Override
    public boolean isValid(Object verificationToken) {
        return this.itemMonitor != null;
    }

    @Override
    public void postChange(IBaseMonitor<IAEItemStack> monitor, Iterable<IAEItemStack> change,
        BaseActionSource actionSource) {
        for (final IAEItemStack is : change) {
            this.items.add(is);
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

    private void fluidHandler(IAEItemStack send) {
        if (this.fluidMonitorObject != null && send.getItem() instanceof ItemCraftingAspect) {
            this.fluidMonitorObject.addItemCraftingAspect(send);
        }
    }

    @Override
    public void processItemList() {
        if (!this.items.isEmpty()) {
            final IItemList<IAEItemStack> monitorCache = this.itemMonitor.getStorageList();
            List<IAEItemStack> toSend = new ArrayList<>();
            for (final IAEItemStack is : this.items) {
                if (is.getItem() instanceof ItemFluidDrop) continue;
                IAEItemStack send = monitorCache.findPrecise(is);
                if (send != null) {
                    fluidHandler(send.copy());
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
    public void queueInventory(ICrafting c) {
        if (Platform.isServer() && c instanceof EntityPlayer && this.itemMonitor != null) {
            final IItemList<IAEItemStack> monitorCache = this.itemMonitor.getStorageList();
            List<IAEItemStack> toSend = new ArrayList<>();
            for (final IAEItemStack is : monitorCache) {
                if (is.getItem() instanceof ItemFluidDrop) continue;
                fluidHandler(is.copy());
                toSend.add(is);
            }
            SPacketMEItemInvUpdate piu = new SPacketMEItemInvUpdate();
            piu.addAll(toSend);
            AE2Thing.proxy.netHandler.sendTo(piu, (EntityPlayerMP) c);
        }
    }

    @Override
    public void removeCraftingFromCrafters(ICrafting c) {
        if (this.crafters.isEmpty() && this.itemMonitor != null) {
            this.itemMonitor.removeListener(this);
        }
    }

    @Override
    public void removeListener() {
        if (this.itemMonitor != null) this.itemMonitor.removeListener(this);
    }

    public IMEMonitor<IAEItemStack> getMonitor() {
        return this.itemMonitor;
    }
}
