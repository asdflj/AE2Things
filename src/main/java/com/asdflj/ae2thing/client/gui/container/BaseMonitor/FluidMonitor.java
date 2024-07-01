package com.asdflj.ae2thing.client.gui.container.BaseMonitor;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ICrafting;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.network.SPacketMEFluidInvUpdate;

import appeng.api.AEApi;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.util.Platform;

public class FluidMonitor implements IMEMonitorHandlerReceiver<IAEFluidStack>, IProcessItemList {

    private IMEMonitor<IAEFluidStack> fluidMonitor;
    private final IItemList<IAEFluidStack> fluids = AEApi.instance()
        .storage()
        .createFluidList();
    private final List<ICrafting> crafters;

    public FluidMonitor(IStorageGrid storageGrid, List<ICrafting> crafters) {
        this.fluidMonitor = storageGrid.getFluidInventory();
        this.crafters = crafters;
    }
    public FluidMonitor(List<ICrafting> crafters) {
        this.crafters = crafters;
    }



    @Override
    public boolean isValid(Object verificationToken) {
        return this.fluidMonitor == null;
    }

    @Override
    public void postChange(IBaseMonitor<IAEFluidStack> monitor, Iterable<IAEFluidStack> change,
        BaseActionSource actionSource) {
        for (final IAEFluidStack is : change) {
            this.fluids.add(is);
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

    @Override
    public void addListener() {
        this.fluidMonitor.addListener(this, null);
    }

    @Override
    public void removeListener() {
        if (this.fluidMonitor != null) this.fluidMonitor.removeListener(this);
    }

    @Override
    public IMEMonitor<IAEFluidStack> getMonitor() {
        return this.fluidMonitor;
    }

    @Override
    public void processItemList() {
        if (!this.fluids.isEmpty()) {
            final IItemList<IAEFluidStack> monitorCache = this.fluidMonitor.getStorageList();
            List<IAEFluidStack> toSend = new ArrayList<>();
            for (final IAEFluidStack is : this.fluids) {
                final IAEFluidStack send = monitorCache.findPrecise(is);
                if (send != null) {
                    toSend.add(send);
                } else {
                    is.setStackSize(0);
                    toSend.add(is);
                }
            }
            SPacketMEFluidInvUpdate piu = new SPacketMEFluidInvUpdate();
            piu.addAll(toSend);
            for (final Object c : this.crafters) {
                if (c instanceof EntityPlayer) {
                    AE2Thing.proxy.netHandler.sendTo(piu, (EntityPlayerMP) c);
                }
            }
            this.fluids.resetStatus();
        }
    }

    @Override
    public void queueInventory(ICrafting c) {
        if (Platform.isServer() && c instanceof EntityPlayer && this.fluidMonitor != null) {
            final IItemList<IAEFluidStack> monitorCache = this.fluidMonitor.getStorageList();
            List<IAEFluidStack> toSend = new ArrayList<>();
            for (final IAEFluidStack is : monitorCache) {
                toSend.add(is);
            }
            SPacketMEFluidInvUpdate piu = new SPacketMEFluidInvUpdate();
            piu.addAll(toSend);
            AE2Thing.proxy.netHandler.sendTo(piu, (EntityPlayerMP) c);
        }
    }

    @Override
    public void removeCraftingFromCrafters(ICrafting c) {
        if (this.crafters.isEmpty() && this.fluidMonitor != null) {
            this.fluidMonitor.removeListener(this);
        }
    }

    public void setMonitor(IMEMonitor<IAEFluidStack> inv) {
        this.fluidMonitor = inv;
    }
}
