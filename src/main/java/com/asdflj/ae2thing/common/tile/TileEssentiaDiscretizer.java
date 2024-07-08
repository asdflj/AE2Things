package com.asdflj.ae2thing.common.tile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import com.asdflj.ae2thing.common.item.ItemPhial;
import com.glodblock.github.crossmod.thaumcraft.AspectUtil;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.events.MENetworkStorageEvent;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.helpers.IPriorityHost;
import appeng.me.GridAccessException;
import appeng.me.cache.CraftingGridCache;
import appeng.me.storage.MEInventoryHandler;
import appeng.tile.grid.AENetworkTile;
import thaumcraft.api.aspects.Aspect;

public class TileEssentiaDiscretizer extends AENetworkTile implements IPriorityHost, ICellContainer {

    private final BaseActionSource ownActionSource = new MachineSource(this);
    private final FluidDiscretizingInventory fluidDropInv = new FluidDiscretizingInventory();
    private final FluidCraftingInventory fluidCraftInv = new FluidCraftingInventory();
    private boolean prevActiveState = false;

    public TileEssentiaDiscretizer() {
        super();
        getProxy().setIdlePowerUsage(3D);
        getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
    }

    @Override
    public boolean canBeRotated() {
        return false;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public List<IMEInventoryHandler> getCellArray(StorageChannel channel) {
        if (getProxy().isActive()) {
            if (channel == StorageChannel.ITEMS) {
                return Collections.singletonList(fluidDropInv.invHandler);
            } else if (channel == StorageChannel.FLUIDS) {
                return Collections.singletonList(fluidCraftInv.invHandler);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void setPriority(int newValue) {
        // do nothing
    }

    @Override
    public void blinkCell(int slot) {
        // do nothing
    }

    @Override
    public void gridChanged() {
        IMEMonitor<IAEFluidStack> fluidGrid = getFluidGrid();
        if (fluidGrid != null) {
            fluidGrid.addListener(fluidDropInv, fluidGrid);
        }
    }

    @Override
    public void saveChanges(IMEInventory cellInventory) {
        markDirty();
    }

    private IMEMonitor<IAEFluidStack> getFluidGrid() {
        try {
            return getProxy().getGrid()
                .<IStorageGrid>getCache(IStorageGrid.class)
                .getFluidInventory();
        } catch (GridAccessException e) {
            return null;
        }
    }

    private IEnergyGrid getEnergyGrid() {
        try {
            return getProxy().getGrid()
                .getCache(IEnergyGrid.class);
        } catch (GridAccessException e) {
            return null;
        }
    }

    private void updateState() {
        boolean isActive = getProxy().isActive();
        if (isActive != prevActiveState) {
            prevActiveState = isActive;
            try {
                getProxy().getGrid()
                    .postEvent(new MENetworkCellArrayUpdate());
            } catch (GridAccessException e) {
                // NO-OP
            }
        }
    }

    @MENetworkEventSubscribe
    public void onPowerUpdate(MENetworkPowerStatusChange event) {
        updateState();
    }

    @MENetworkEventSubscribe
    public void onChannelUpdate(MENetworkChannelsChanged event) {
        updateState();
    }

    @MENetworkEventSubscribe
    public void onStorageUpdate(MENetworkStorageEvent event) {
        updateState();
    }

    private class FluidDiscretizingInventory
        implements IMEInventory<IAEItemStack>, IMEMonitorHandlerReceiver<IAEFluidStack> {

        private final MEInventoryHandler<IAEItemStack> invHandler = new MEInventoryHandler<>(this, getChannel());
        private IItemList<IAEItemStack> itemCache = null;

        FluidDiscretizingInventory() {
            invHandler.setPriority(Integer.MAX_VALUE);
        }

        @Override
        public IAEItemStack injectItems(IAEItemStack request, Actionable type, BaseActionSource src) {
            Aspect aspect = ItemPhial.getAspect(request);
            if (aspect == null) {
                return request;
            }
            IMEMonitor<IAEFluidStack> fluidGrid = getFluidGrid();
            if (fluidGrid == null) {
                return request;
            }
            IEnergyGrid energyGrid = getEnergyGrid();
            if (energyGrid == null) {
                return request;
            }
            return ItemPhial.newAeStack(
                fluidGrid.injectItems(ItemPhial.newEssentiaStack(aspect, request.getStackSize()), type, src));
        }

        @Override
        public IAEItemStack extractItems(IAEItemStack request, Actionable type, BaseActionSource src) {
            Aspect aspect = ItemPhial.getAspect(request);
            if (aspect == null) {
                return null;
            }
            IMEMonitor<IAEFluidStack> fluidGrid = getFluidGrid();
            if (fluidGrid == null) {
                return null;
            }
            IEnergyGrid energyGrid = getEnergyGrid();
            if (energyGrid == null) {
                return null;
            }
            return ItemPhial.newAeStack(
                fluidGrid.extractItems(ItemPhial.newEssentiaStack(aspect, request.getStackSize()), type, src));
        }

        @Override
        public IItemList<IAEItemStack> getAvailableItems(IItemList<IAEItemStack> out) {
            if (itemCache == null) {
                itemCache = AEApi.instance()
                    .storage()
                    .createItemList();
                IMEMonitor<IAEFluidStack> fluidGrid = getFluidGrid();
                if (fluidGrid != null) {
                    for (IAEFluidStack fluid : fluidGrid.getStorageList()) {
                        IAEItemStack stack = ItemPhial.newAeStack(fluid);
                        if (stack != null) {
                            itemCache.add(stack);
                        }
                    }
                }
            }
            for (IAEItemStack stack : itemCache) {
                out.addStorage(stack);
            }
            return out;
        }

        @Override
        public IAEItemStack getAvailableItem(@Nonnull IAEItemStack request) {
            IMEMonitor<IAEFluidStack> fluidGrid = getFluidGrid();
            if (fluidGrid == null) {
                return null;
            }
            IAEFluidStack fluidRequest = ItemPhial.getAeEssentiaStack(request);
            if (fluidRequest == null) {
                return null;
            }
            IAEFluidStack availableFluid = fluidGrid.getAvailableItem(fluidRequest);
            if (availableFluid == null || availableFluid.getFluid() == null) {
                return null;
            }
            return ItemPhial.newAeStack(availableFluid);
        }

        @Override
        public StorageChannel getChannel() {
            return StorageChannel.ITEMS;
        }

        @Override
        public boolean isValid(Object verificationToken) {
            IMEMonitor<IAEFluidStack> fluidGrid = getFluidGrid();
            return fluidGrid != null && fluidGrid == verificationToken;
        }

        @Override
        public void postChange(IBaseMonitor<IAEFluidStack> monitor, Iterable<IAEFluidStack> change,
            BaseActionSource actionSource) {
            itemCache = null;
            try {
                List<IAEItemStack> mappedChanges = new ArrayList<>();
                for (IAEFluidStack fluidStack : change) {
                    IAEItemStack itemStack = ItemPhial.newAeStack(fluidStack);
                    if (itemStack != null) {
                        mappedChanges.add(itemStack);
                    }
                }
                getProxy().getGrid()
                    .<IStorageGrid>getCache(IStorageGrid.class)
                    .postAlterationOfStoredItems(getChannel(), mappedChanges, ownActionSource);
            } catch (GridAccessException e) {
                // NO-OP
            }
        }

        @Override
        public void onListUpdate() {
            // NO-OP
        }
    }

    private class FluidCraftingInventory implements IMEInventory<IAEFluidStack> {

        private final MEInventoryHandler<IAEFluidStack> invHandler = new MEInventoryHandler<>(this, getChannel());

        FluidCraftingInventory() {
            invHandler.setPriority(Integer.MAX_VALUE);
        }

        @Override
        @SuppressWarnings("rawtypes")
        public IAEFluidStack injectItems(IAEFluidStack input, Actionable type, BaseActionSource src) {
            ICraftingGrid craftingGrid;
            try {
                craftingGrid = getProxy().getGrid()
                    .getCache(ICraftingGrid.class);
            } catch (GridAccessException e) {
                return null;
            }
            if (craftingGrid instanceof CraftingGridCache) {
                if (AspectUtil.isEssentiaGas(input.getFluidStack())) {
                    IAEStack remaining = ((CraftingGridCache) craftingGrid)
                        .injectItems(ItemPhial.newAeStack(input), type, ownActionSource);
                    if (remaining instanceof IAEItemStack) {
                        return ItemPhial.getAeEssentiaStack((IAEItemStack) remaining);
                    }
                }
            }
            return input;
        }

        @Override
        public IAEFluidStack extractItems(IAEFluidStack request, Actionable mode, BaseActionSource src) {
            return null;
        }

        @Override
        public IItemList<IAEFluidStack> getAvailableItems(IItemList<IAEFluidStack> out) {
            return out;
        }

        @Override
        public IAEFluidStack getAvailableItem(@Nonnull IAEFluidStack request) {
            return null;
        }

        @Override
        public StorageChannel getChannel() {
            return StorageChannel.FLUIDS;
        }
    }
}
