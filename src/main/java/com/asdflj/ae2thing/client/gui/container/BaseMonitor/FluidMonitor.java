package com.asdflj.ae2thing.client.gui.container.BaseMonitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ICrafting;
import net.minecraftforge.fluids.FluidStack;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.network.SPacketMEFluidInvUpdate;
import com.glodblock.github.common.item.ItemFluidDrop;

import appeng.api.AEApi;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.Platform;
import appeng.util.item.AEFluidStack;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.common.fluids.GaseousEssentia;
import thaumicenergistics.common.items.ItemCraftingAspect;

public class FluidMonitor implements IMEMonitorHandlerReceiver<IAEFluidStack>, IProcessItemList {

    private IMEMonitor<IAEFluidStack> fluidMonitor;
    private IMEMonitor<IAEItemStack> itemMonitor;
    private final IItemList<IAEFluidStack> fluids = AEApi.instance()
        .storage()
        .createFluidList();
    private final Set<IAEItemStack> craftingAspects = new HashSet<>();
    private final Set<IAEItemStack> craftingfluids = new HashSet<>();
    private final List<ICrafting> crafters;
    private final List<IAEFluidStack> toSend = new ArrayList<>();

    public FluidMonitor(IStorageGrid storageGrid, List<ICrafting> crafters) {
        this.fluidMonitor = storageGrid.getFluidInventory();
        this.itemMonitor = storageGrid.getItemInventory();
        this.crafters = crafters;
    }

    public FluidMonitor(List<ICrafting> crafters) {
        this.crafters = crafters;
    }

    @Override
    public boolean isValid(Object verificationToken) {
        return this.fluidMonitor != null;
    }

    public void addItemCraftingAspect(IAEItemStack is) {
        craftingAspects.add(is);
    }

    public void addItemCraftingFluids(IAEItemStack is) {
        craftingfluids.add(is);
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
        SPacketMEFluidInvUpdate piu = new SPacketMEFluidInvUpdate();
        if (!this.fluids.isEmpty()) {
            final IItemList<IAEFluidStack> monitorCache = this.fluidMonitor.getStorageList();
            final IItemList<IAEItemStack> itemMonitorCache = this.itemMonitor.getStorageList();
            for (final IAEFluidStack is : this.fluids) {
                IAEFluidStack send = monitorCache.findPrecise(is);
                if (send != null) {
                    IAEItemStack fluidDrop = itemMonitorCache.findPrecise(ItemFluidDrop.newAeStack(send));
                    if (fluidDrop != null) {
                        send = send.copy();
                        send.setCraftable(fluidDrop.isCraftable());
                    }
                    toSend.add(send);
                } else {
                    is.setStackSize(0);
                    toSend.add(is);
                }
            }
            piu.addAll(toSend);
            this.fluids.resetStatus();
        }
        if (!this.craftingAspects.isEmpty()) {
            final IItemList<IAEFluidStack> monitorCache = this.fluidMonitor.getStorageList();
            for (IAEItemStack is : this.craftingAspects) {
                Aspect aspect = ItemCraftingAspect.getAspect(is.getItemStack());
                GaseousEssentia gaseousEssentia = GaseousEssentia.getGasFromAspect(aspect);
                IAEFluidStack fs = AEFluidStack.create(new FluidStack(gaseousEssentia, 1));
                IAEFluidStack send = monitorCache.findPrecise(fs);
                if (send != null) {
                    send.setCraftable(is.isCraftable());
                    toSend.add(send);
                } else {
                    fs.setStackSize(0);
                    fs.setCraftable(is.isCraftable());
                    toSend.add(fs);
                }
            }
            piu.addAll(toSend);
            this.craftingAspects.clear();
        }
        if (!this.craftingfluids.isEmpty()) {
            for (IAEItemStack is : this.craftingfluids) {
                IAEFluidStack fs = ItemFluidDrop.getAeFluidStack(is);
                if (fs == null) continue;
                fs.setCraftable(is.isCraftable());
                toSend.add(fs);
            }
            piu.addAll(toSend);
            this.craftingfluids.clear();
        }
        if (!piu.isEmpty()) {
            for (final Object c : this.crafters) {
                if (c instanceof EntityPlayer) {
                    AE2Thing.proxy.netHandler.sendTo(piu, (EntityPlayerMP) c);
                }
            }
        }
        toSend.clear();
    }

    @Override
    public void queueInventory(ICrafting c) {
        if (Platform.isServer() && c instanceof EntityPlayer && this.fluidMonitor != null && this.itemMonitor != null) {
            final IItemList<IAEFluidStack> monitorCache = this.fluidMonitor.getStorageList();
            final IItemList<IAEItemStack> itemMonitorCache = this.itemMonitor.getStorageList();
            List<IAEFluidStack> toSend = new ArrayList<>();
            for (final IAEFluidStack is : monitorCache) {
                final IAEFluidStack send = is.copy();
                IAEItemStack fluidDrop = itemMonitorCache.findPrecise(ItemFluidDrop.newAeStack(is));
                if (fluidDrop != null) {
                    send.setCraftable(fluidDrop.isCraftable());
                }
                toSend.add(send);
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

    public void setMonitor(IMEMonitor<IAEFluidStack> fluidMonitor, IMEMonitor<IAEItemStack> itemMonitor) {
        this.fluidMonitor = fluidMonitor;
        this.itemMonitor = itemMonitor;
    }
}
