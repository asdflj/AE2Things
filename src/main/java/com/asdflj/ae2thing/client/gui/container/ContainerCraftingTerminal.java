package com.asdflj.ae2thing.client.gui.container;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;

import appeng.api.config.Actionable;
import appeng.api.storage.data.IAEFluidStack;
import appeng.util.item.AEFluidStack;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.network.SPacketFluidUpdate;
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
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

public class ContainerCraftingTerminal extends AEBaseContainer implements IConfigurableObject, IConfigManagerHost,
    IMEMonitorHandlerReceiver<IAEItemStack>, IAEAppEngInventory, IContainerCraftingPacket {

    protected final PortableItemInventory it;
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

    public void postChange(IAEFluidStack fluid, EntityPlayer player, int slotIndex, boolean shift) {
        ItemStack targetStack;
        if (slotIndex == -1) {
            targetStack = player.inventory.getItemStack();
        } else {
            targetStack = player.inventory.getStackInSlot(slotIndex);
        }
        // The primary output itemstack
        if (com.glodblock.github.util.Util.FluidUtil.isEmpty(targetStack) && fluid != null) {
            // Situation 1.a: Empty fluid container, and nonnull slot
            extractFluid(fluid, player, slotIndex, shift);
        } else if (!com.glodblock.github.util.Util.FluidUtil.isEmpty(targetStack)) {
            // Situation 2.a: We are holding a non-empty container.
            insertFluid(player, slotIndex, shift);
            // End of situation 2.a
        }
        // No op (Any other situation)
        this.detectAndSendChanges();
    }

    protected void dropItem(ItemStack is) {
        if (is == null || is.stackSize <= 0) return;
        ItemStack itemStack = is.copy();
        int i = itemStack.getMaxStackSize();
        while (itemStack.stackSize > 0) {
            if (i > itemStack.stackSize) {
                if (!getPlayerInv().addItemStackToInventory(itemStack.copy())) {
                    getPlayerInv().player.entityDropItem(itemStack.copy(), 0);
                }
                break;
            } else {
                itemStack.stackSize -= i;
                ItemStack item = itemStack.copy();
                item.stackSize = i;
                if (!getPlayerInv().addItemStackToInventory(item)) {
                    getPlayerInv().player.entityDropItem(item, 0);
                }
            }
        }
    }

    protected void dropItem(ItemStack itemStack, int stackSize) {
        if (itemStack == null || itemStack.stackSize <= 0) return;
        ItemStack is = itemStack.copy();
        is.stackSize = stackSize;
        this.dropItem(is);
    }
    protected void adjustStack(ItemStack stack) {
        if (stack != null && stack.stackSize > stack.getMaxStackSize()) {
            dropItem(stack, stack.stackSize - stack.getMaxStackSize());
            stack.stackSize = stack.getMaxStackSize();
        }
    }
    /**
     * The insert operation. For input, we have a filled container stack. For outputs, we have the following:
     * <ol>
     * <li>Leftover filled container stack</li>
     * <li>Empty containers</li>
     * <li>Partially filled container x1</li>
     * </ol>
     * In order above, the itemstack at `slotIndex` is transformed into the output.
     */
    private void insertFluid(EntityPlayer player, int slotIndex, boolean shift) {
        final ItemStack targetStack;
        if (slotIndex == -1) {
            targetStack = player.inventory.getItemStack();
        } else {
            targetStack = player.inventory.getStackInSlot(slotIndex);
        }
        final int containersRequestedToInsert = shift ? targetStack.stackSize : 1;

        // Step 1: Determine container characteristics and verify fluid to be extractable
        final int fluidPerContainer;
        final FluidStack fluidStackPerContainer;
        final boolean partialInsertSupported;
        if (targetStack.getItem() instanceof IFluidContainerItem fcItem) {
            ItemStack test = targetStack.copy();
            test.stackSize = 1;
            fluidStackPerContainer = fcItem.drain(test, Integer.MAX_VALUE, false);
            if (fluidStackPerContainer == null || fluidStackPerContainer.amount == 0) {
                return;
            }

            fluidPerContainer = fluidStackPerContainer.amount;
            partialInsertSupported = true;
        } else if (FluidContainerRegistry.isContainer(targetStack)) {
            ItemStack emptyTank = FluidContainerRegistry.drainFluidContainer(targetStack);
            if (emptyTank == null) {
                return;
            }

            fluidStackPerContainer = FluidContainerRegistry.getFluidForFilledItem(targetStack);
            fluidPerContainer = fluidStackPerContainer.amount;
            partialInsertSupported = false;
        } else {
            return;
        }

        // Step 2: determine network capacity
        final IAEFluidStack totalFluid = AEFluidStack.create(fluidStackPerContainer);
        totalFluid.setStackSize((long) fluidPerContainer * containersRequestedToInsert);

        final IAEFluidStack notInsertable = this.host.getFluidInventory()
            .injectItems(totalFluid, Actionable.SIMULATE, this.getActionSource());

        final long insertableFluid;
        if (notInsertable == null || notInsertable.getStackSize() == 0) {
            insertableFluid = totalFluid.getStackSize();
        } else {
            long insertable = totalFluid.getStackSize() - notInsertable.getStackSize();
            if (partialInsertSupported) {
                insertableFluid = insertable;
            } else {
                // avoid remainder
                insertableFluid = insertable - (insertable % fluidPerContainer);
            }
        }
        totalFluid.setStackSize(insertableFluid);

        // Step 3: perform insert
        final long totalInserted;
        final IAEFluidStack notInserted = this.host.getFluidInventory()
            .injectItems(totalFluid, Actionable.MODULATE, this.getActionSource());
        if (notInserted != null && notInserted.getStackSize() > 0) {
            // User has a setup that causes discrepancy between simulation and modulation. Likely double storage bus.
            long total = totalFluid.getStackSize() - notInserted.getStackSize();
            if (partialInsertSupported) {
                totalInserted = total;
            } else {
                // We cant have partially filled containers -> user will receive a fluid packet as last resort
                long overflowAmount = fluidPerContainer - (total % fluidPerContainer);
                IAEFluidStack overflow = AEFluidStack.create(fluidStackPerContainer);
                overflow.setStackSize(overflowAmount);
                dropItem(ItemFluidPacket.newStack(overflow));
                totalInserted = total + overflowAmount;
            }
        } else {
            totalInserted = totalFluid.getStackSize();
        }

        // Step 4: calculate outputs
        final int emptiedTanks = (int) (totalInserted / fluidPerContainer);
        final int partialDrain = (int) (totalInserted % fluidPerContainer);
        final int partialTanks = partialDrain > 0 && partialInsertSupported ? 1 : 0;
        final int usedTanks = emptiedTanks + partialTanks;
        final int untouchedTanks = targetStack.stackSize - usedTanks;

        final ItemStack emptiedTanksStack;
        final ItemStack partialTanksStack;

        if (targetStack.getItem() instanceof IFluidContainerItem fcItem) {
            if (emptiedTanks > 0) {
                emptiedTanksStack = targetStack.copy();
                emptiedTanksStack.stackSize = 1;
                fcItem.drain(emptiedTanksStack, fluidPerContainer, true);
                emptiedTanksStack.stackSize = emptiedTanks;
            } else {
                emptiedTanksStack = null;
            }
            if (partialTanks > 0) {
                partialTanksStack = targetStack.copy();
                partialTanksStack.stackSize = 1;
                fcItem.drain(partialTanksStack, partialDrain, true);
            } else {
                partialTanksStack = null;
            }
        } else {
            if (emptiedTanks > 0) {
                emptiedTanksStack = FluidContainerRegistry.drainFluidContainer(targetStack);
                emptiedTanksStack.stackSize = emptiedTanks;
            } else {
                emptiedTanksStack = null;
            }
            // Not possible > see Step 2 and Step 3
            partialTanksStack = null;
        }

        // Done. Put the output in the inventory or ground, and update stack size.
        boolean shouldSendStack = true;
        if (slotIndex == -1) {
            // Item is in mouse hand
            if (untouchedTanks > 0) {
                targetStack.stackSize = untouchedTanks;
                adjustStack(targetStack);
                dropItem(emptiedTanksStack);
                dropItem(partialTanksStack);
            } else if (emptiedTanksStack != null) {
                adjustStack(emptiedTanksStack);
                player.inventory.setItemStack(emptiedTanksStack);
                dropItem(partialTanksStack);
            } else if (partialTanksStack != null) {
                player.inventory.setItemStack(partialTanksStack);
            } else {
                player.inventory.setItemStack(null);
                shouldSendStack = false;
            }
        } else {
            // Shift clicked in
            if (untouchedTanks > 0) {
                targetStack.stackSize = untouchedTanks;
                adjustStack(targetStack);
                dropItem(emptiedTanksStack);
                dropItem(partialTanksStack);
            } else if (emptiedTanksStack != null) {
                adjustStack(emptiedTanksStack);
                player.inventory.setInventorySlotContents(slotIndex, emptiedTanksStack);
                dropItem(partialTanksStack);
            } else if (partialTanksStack != null) {
                player.inventory.setInventorySlotContents(slotIndex, partialTanksStack);
            } else {
                player.inventory.setItemStack(null);
                shouldSendStack = false;
            }
        }
        if (shouldSendStack) {
            FluidCraft.proxy.netHandler.sendTo(
                new SPacketFluidUpdate(new HashMap<>(), player.inventory.getItemStack()),
                (EntityPlayerMP) player);
        } else {
            FluidCraft.proxy.netHandler.sendTo(new SPacketFluidUpdate(new HashMap<>()), (EntityPlayerMP) player);
        }
    }

    /**
     * The extract operation. For input, we have an empty container stack. For outputs, we have the following:
     * <ol>
     * <li>Leftover empty container stack</li>
     * <li>Filled containers (full)</li>
     * <li>Partially filled container x1</li>
     * </ol>
     * In order above, the itemstack at `slotIndex` is transformed into the output.
     */
    private void extractFluid(IAEFluidStack clientRequestedFluid, EntityPlayer player, int slotIndex, boolean shift) {
        if (slotIndex != -1) {
            // shift-click from inventory cant fill fluids
            return;
        }
        final ItemStack targetStack = player.inventory.getItemStack();
        final int containersRequestedToExtract = shift ? targetStack.stackSize : 1;

        final FluidStack clientRequestedFluidStack = clientRequestedFluid.getFluidStack();
        clientRequestedFluidStack.amount = Integer.MAX_VALUE;

        // Step 1: Determine container characteristics and verify fluid to be insertable
        final int fluidPerContainer;
        final boolean partialInsertSupported;
        if (targetStack.getItem() instanceof IFluidContainerItem fcItem) {
            ItemStack testStack = targetStack.copy();
            testStack.stackSize = 1;
            fluidPerContainer = fcItem.fill(testStack, clientRequestedFluidStack, false);
            if (fluidPerContainer == 0) {
                return;
            }
            partialInsertSupported = true;
        } else if (FluidContainerRegistry.isContainer(targetStack)) {
            fluidPerContainer = FluidContainerRegistry.getContainerCapacity(clientRequestedFluidStack, targetStack);
            partialInsertSupported = false;
        } else {
            return;
        }

        // Step 2: determine fluid in network
        final IAEFluidStack totalRequestedFluid = clientRequestedFluid.copy();
        totalRequestedFluid.setStackSize((long) fluidPerContainer * containersRequestedToExtract);

        final IAEFluidStack availableFluid = this.host.getFluidInventory()
            .extractItems(totalRequestedFluid, Actionable.SIMULATE, this.getActionSource());
        if (availableFluid == null || availableFluid.getStackSize() == 0) {
            return;
        }

        if (availableFluid.getStackSize() != totalRequestedFluid.getStackSize() && !partialInsertSupported) {
            availableFluid.decStackSize(availableFluid.getStackSize() % fluidPerContainer);
        }

        // Step 3: perform extract
        final IAEFluidStack extracted = this.host.getFluidInventory()
            .extractItems(availableFluid, Actionable.MODULATE, this.getActionSource());
        final long totalExtracted = extracted != null ? extracted.getStackSize() : 0;

        // Step 4: calculate outputs
        final int filledTanks = (int) (totalExtracted / fluidPerContainer);
        final int partialFill = (int) (totalExtracted % fluidPerContainer);
        final int partialTanks = partialFill > 0 && partialInsertSupported ? 1 : 0;
        final int usedTanks = filledTanks + partialTanks;
        final int untouchedTanks = targetStack.stackSize - usedTanks;

        final ItemStack filledTanksStack;
        final ItemStack partialTanksStack;

        if (targetStack.getItem() instanceof IFluidContainerItem fcItem) {
            if (filledTanks > 0) {
                filledTanksStack = targetStack.copy();
                filledTanksStack.stackSize = 1;
                FluidStack toInsert = extracted.getFluidStack().copy();
                toInsert.amount = fluidPerContainer;
                fcItem.fill(filledTanksStack, toInsert, true);
                filledTanksStack.stackSize = filledTanks;
            } else {
                filledTanksStack = null;
            }
            if (partialTanks > 0) {
                partialTanksStack = targetStack.copy();
                partialTanksStack.stackSize = 1;
                FluidStack toInsert = extracted.getFluidStack().copy();
                toInsert.amount = partialFill;
                fcItem.fill(partialTanksStack, toInsert, true);
            } else {
                partialTanksStack = null;
            }
        } else {
            if (filledTanks > 0) {
                FluidStack toInsert = extracted.getFluidStack().copy();
                toInsert.amount = fluidPerContainer;
                filledTanksStack = FluidContainerRegistry.fillFluidContainer(toInsert, targetStack);
                filledTanksStack.stackSize = filledTanks;
            } else {
                filledTanksStack = null;
            }
            if (partialFill > 0) {
                // User has a setup that causes discrepancy between simulation and modulation. Likely double storage
                // bus.
                // We cant have partially filled containers -> user will receive a fluid packet as last resort
                IAEFluidStack overflow = extracted.copy();
                overflow.setStackSize(partialFill);
                dropItem(ItemFluidPacket.newStack(overflow));
            }
            partialTanksStack = null;
        }

        // Done. Put the output in the inventory or ground, and update stack size.
        // We can assume slotIndex == -1, since we don't actually allow extraction via shift click.
        boolean shouldSendStack = true;
        if (untouchedTanks > 0) {
            ItemStack emptyStack = player.inventory.getItemStack();
            emptyStack.stackSize = untouchedTanks;
            adjustStack(emptyStack);
            dropItem(filledTanksStack);
            dropItem(partialTanksStack);
        } else if (filledTanksStack != null) {
            adjustStack(filledTanksStack);
            player.inventory.setItemStack(filledTanksStack);
            dropItem(partialTanksStack);
        } else if (partialTanksStack != null) {
            player.inventory.setItemStack(partialTanksStack);
        } else {
            player.inventory.setItemStack(null);
            shouldSendStack = false;
        }
        if (shouldSendStack) {
            FluidCraft.proxy.netHandler.sendTo(
                new SPacketFluidUpdate(new HashMap<>(), player.inventory.getItemStack()),
                (EntityPlayerMP) player);
        } else {
            FluidCraft.proxy.netHandler.sendTo(new SPacketFluidUpdate(new HashMap<>()), (EntityPlayerMP) player);
        }
    }

}
