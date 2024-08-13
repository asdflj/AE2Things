package com.asdflj.ae2thing.common.tile;

import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.common.Config;
import com.asdflj.ae2thing.util.Ae2Reflect;

import appeng.api.config.Actionable;
import appeng.api.config.OperationMode;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.events.MENetworkBootingStatusChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.me.GridAccessException;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.storage.TileIOPort;
import io.netty.buffer.ByteBuf;

public class TileExIOPort extends TileIOPort implements IPowerChannelState {

    protected long FLUID_MULTIPLIER = Ae2Reflect.getFluidMultiplier(this);

    private boolean isPowered = false;

    @Override
    public boolean isPowered() {
        return isPowered;
    }

    public boolean isActive() {
        return isPowered;
    }

    @TileEvent(TileEventType.NETWORK_READ)
    public boolean readFromStream(final ByteBuf data) {
        final boolean oldPower = isPowered;
        isPowered = data.readBoolean();
        return isPowered != oldPower;
    }

    @TileEvent(TileEventType.NETWORK_WRITE)
    public void writeToStream(final ByteBuf data) {
        data.writeBoolean(isActive());
    }

    @MENetworkEventSubscribe
    public void stateChange(final MENetworkPowerStatusChange p) {
        updatePowerState();
    }

    @MENetworkEventSubscribe
    public final void bootingRender(final MENetworkBootingStatusChange c) {
        updatePowerState();
    }

    private void updatePowerState() {
        boolean newState = false;

        try {
            newState = getProxy().isActive() && getProxy().getEnergy()
                .extractAEPower(1, Actionable.SIMULATE, PowerMultiplier.CONFIG) > 0.0001;
        } catch (final GridAccessException ignored) {

        }
        if (newState != isPowered) {
            isPowered = newState;
            markForUpdate();
        }
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (!this.getProxy()
            .isActive()) {
            return TickRateModulation.IDLE;
        }

        long ItemsToMove = 256L * Config.exIOPortTransferContentsRate;

        switch (this.getInstalledUpgrades(Upgrades.SPEED)) {
            case 1 -> ItemsToMove *= 2;
            case 2 -> ItemsToMove *= 4;
            case 3 -> ItemsToMove *= 8;
        }

        switch (this.getInstalledUpgrades(Upgrades.SUPERSPEED)) {
            case 1 -> ItemsToMove *= 16;
            case 2 -> ItemsToMove *= 128;
            case 3 -> ItemsToMove *= 1024;
        }

        try {
            final IMEInventory<IAEItemStack> itemNet = this.getProxy()
                .getStorage()
                .getItemInventory();
            final IMEInventory<IAEFluidStack> fluidNet = this.getProxy()
                .getStorage()
                .getFluidInventory();
            final IEnergySource energy = this.getProxy()
                .getEnergy();
            for (int x = 0; x < 6; x++) {
                final ItemStack is = Ae2Reflect.getCells(this)
                    .getStackInSlot(x);
                if (is != null) {
                    if (ItemsToMove > 0) {
                        final IMEInventory<IAEItemStack> itemInv = Ae2Reflect.getInv(this, is, StorageChannel.ITEMS);
                        final IMEInventory<IAEFluidStack> fluidInv = Ae2Reflect.getInv(this, is, StorageChannel.FLUIDS);

                        if (Ae2Reflect.getManager(this)
                            .getSetting(Settings.OPERATION_MODE) == OperationMode.EMPTY) {
                            if (itemInv != null) {
                                ItemsToMove = Ae2Reflect.transferContents(
                                    this,
                                    energy,
                                    itemInv,
                                    itemNet,
                                    ItemsToMove,
                                    StorageChannel.ITEMS);
                            }
                            if (fluidInv != null) {
                                ItemsToMove = Ae2Reflect.transferContents(
                                    this,
                                    energy,
                                    fluidInv,
                                    fluidNet,
                                    ItemsToMove * FLUID_MULTIPLIER,
                                    StorageChannel.FLUIDS);
                            }
                        } else {
                            if (itemInv != null) {
                                ItemsToMove = Ae2Reflect.transferContents(
                                    this,
                                    energy,
                                    itemNet,
                                    itemInv,
                                    ItemsToMove,
                                    StorageChannel.ITEMS);
                            }
                            if (fluidInv != null) {
                                ItemsToMove = Ae2Reflect.transferContents(
                                    this,
                                    energy,
                                    fluidNet,
                                    fluidInv,
                                    ItemsToMove * FLUID_MULTIPLIER,
                                    StorageChannel.FLUIDS);
                            }
                        }

                        if (ItemsToMove > 0 && Ae2Reflect.shouldMove(this, itemInv, fluidInv)
                            && !Ae2Reflect.moveSlot(this, x)) {
                            return TickRateModulation.IDLE;
                        }

                        return TickRateModulation.URGENT;
                    } else {
                        return TickRateModulation.URGENT;
                    }
                }
            }
        } catch (final GridAccessException e) {
            return TickRateModulation.IDLE;
        }

        // nothing left to do...
        return TickRateModulation.SLEEP;
    }
}
