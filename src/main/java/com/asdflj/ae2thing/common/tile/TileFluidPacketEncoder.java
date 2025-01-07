package com.asdflj.ae2thing.common.tile;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.inventory.item.FakeAEInventory;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.util.Util;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.GridFlags;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;
import appeng.util.item.AEFluidStack;
import io.netty.buffer.ByteBuf;

public class TileFluidPacketEncoder extends AENetworkInvTile
    implements IPowerChannelState, IStackWatcherHost, IAEAppEngInventory {

    private boolean isPowered = false;
    private final AppEngInternalAEInventory config = new AppEngInternalAEInventory(this, 1);
    private final FakeAEInventory output = new FakeAEInventory(this, 1);
    private IStackWatcher myWatcher;
    private long value = 0;
    private final MachineSource source = new MachineSource(this);

    public TileFluidPacketEncoder() {
        getProxy().setIdlePowerUsage(3D);
        getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
    }

    public long getReportingValue() {
        return this.value;
    }

    public void setReportingValue(final long v) {
        this.value = v;
    }

    public void setFluidInSlot(Integer id, IAEFluidStack fluid) {
        ItemStack tmp = ItemFluidPacket.newDisplayStack(fluid == null ? null : fluid.getFluidStack());
        this.config.setInventorySlotContents(id, tmp);
    }

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
    public void channelChanged(final MENetworkChannelsChanged c) {
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
            configureWatchers();
            markForUpdate();
        }
    }

    private IAEFluidStack getIAEFluidStack() {
        final FluidStack fs = ItemFluidPacket.getFluidStack(this.config.getAEStackInSlot(0));
        if (fs != null) return Util.FluidUtil.createAEFluidStack(fs);
        return null;
    }

    @Override
    public boolean dropItems() {
        return false;
    }

    // update the system...
    public void configureWatchers() {
        if (!this.isActive()) {
            output.setInventorySlotContents(0, null);
            return;
        }
        final IAEFluidStack myStack = this.getIAEFluidStack();

        if (this.myWatcher != null) {
            this.myWatcher.clear();
        }

        if (this.myWatcher != null && myStack != null) {
            this.myWatcher.add(myStack);
            updatePacketStack();
        } else {
            output.setInventorySlotContents(0, null);
        }
    }

    @Override
    public void updateWatcher(IStackWatcher newWatcher) {
        this.myWatcher = newWatcher;
        this.configureWatchers();
    }

    @Override
    public void onStackChange(IItemList o, IAEStack fullStack, IAEStack diffStack, BaseActionSource src,
        StorageChannel chan) {
        updatePacketStack();
    }

    private void updatePacketStack() {
        IAEFluidStack fs = getIAEFluidStack();
        if (fs == null || fs.getStackSize() == 0 || this.value <= 0) return;
        fs.setStackSize(this.value);
        ItemStack is = ItemFluidPacket.newStack(fs);
        if (is == null) return;
        try {
            IItemList<IAEFluidStack> storageList = this.getProxy()
                .getStorage()
                .getFluidInventory()
                .getStorageList();
            IAEFluidStack stored = storageList.findPrecise(fs);
            if (stored == null) {
                output.setInventorySlotContentsNoCallBack(0, null);
                return;
            }
            is.stackSize = (stored.getStackSize() / this.value) > Integer.MAX_VALUE ? Integer.MAX_VALUE
                : (int) (stored.getStackSize() / this.value);
        } catch (Exception ignored) {
            is.stackSize = 0;
        }
        output.setInventorySlotContentsNoCallBack(0, is);
    }

    @Override
    public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removedStack,
        ItemStack newStack) {
        if (inv == this.config) {
            this.configureWatchers();
        } else if (inv == this.output && removedStack != null && removedStack.stackSize > 0) {
            try {
                IAEFluidStack fs = AEFluidStack.create(ItemFluidPacket.getFluidStack(removedStack));
                if (fs != null) {
                    fs.setStackSize(fs.getStackSize() * removedStack.stackSize);
                    this.getProxy()
                        .getStorage()
                        .getFluidInventory()
                        .extractItems(fs, Actionable.MODULATE, source);
                }
            } catch (Exception ignored) {

            }
        }
    }

    @Override
    public boolean canInsertItem(int slotIndex, ItemStack insertingItem, int side) {
        return false;
    }

    @Override
    public int[] getAccessibleSlotsBySide(ForgeDirection whichSide) {
        return new int[] { 0 };
    }

    @Override
    public boolean canBeRotated() {
        return false;
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    @Override
    public AECableType getCableConnectionType(ForgeDirection dir) {
        return AECableType.SMART;
    }

    @Override
    public IInventory getInternalInventory() {
        return output;
    }

    public IInventory getInventoryByName(String name) {
        if (name.equals(Constants.CONFIG_INV)) {
            return this.config;
        }
        return null;
    }

    @Override
    public boolean canExtractItem(int slotIndex, ItemStack extractedItem, int side) {
        if (slotIndex == 0 && extractedItem != null
            && this.output.getStackInSlot(0) != null
            && Platform.isSameItemPrecise(extractedItem, this.output.getStackInSlot(0))) {
            ItemStack is = this.output.getStackInSlot(0);
            return is.stackSize >= extractedItem.stackSize;
        }
        return false;
    }

    @TileEvent(TileEventType.WORLD_NBT_READ)
    public void readFromNBTEvent(NBTTagCompound data) {
        this.value = data.getLong("value");
        this.config.readFromNBT(data, Constants.CONFIG_INV);
    }

    @TileEvent(TileEventType.WORLD_NBT_WRITE)
    public NBTTagCompound writeToNBTEvent(NBTTagCompound data) {
        data.setLong("value", value);
        this.config.writeToNBT(data, Constants.CONFIG_INV);
        return data;
    }

}
