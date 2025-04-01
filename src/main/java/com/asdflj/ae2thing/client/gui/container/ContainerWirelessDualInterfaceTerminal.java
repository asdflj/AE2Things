package com.asdflj.ae2thing.client.gui.container;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.asdflj.ae2thing.client.gui.container.slot.SlotPatternFake;
import com.asdflj.ae2thing.client.gui.container.widget.IWidgetPatternContainer;
import com.asdflj.ae2thing.client.gui.container.widget.PatternContainer;
import com.asdflj.ae2thing.common.item.ItemPatternModifier;
import com.asdflj.ae2thing.inventory.IPatternTerminal;
import com.asdflj.ae2thing.inventory.item.INetworkTerminal;
import com.asdflj.ae2thing.inventory.item.PatternModifierInventory;
import com.asdflj.ae2thing.inventory.item.WirelessTerminal;
import com.asdflj.ae2thing.util.Ae2Reflect;
import com.asdflj.ae2thing.util.GTUtil;
import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.util.Util;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.ITerminalHost;
import appeng.api.util.IConfigurableObject;
import appeng.api.util.IInterfaceViewable;
import appeng.container.guisync.GuiSync;
import appeng.container.implementations.ContainerInterfaceTerminal;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.container.slot.SlotPatternOutputs;
import appeng.container.slot.SlotPatternTerm;
import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInterfaceTerminalUpdate;
import appeng.helpers.IContainerCraftingPacket;
import appeng.helpers.InventoryAction;
import appeng.me.cache.CraftingGridCache;
import appeng.me.helpers.ChannelPowerSrc;
import appeng.tile.inventory.InvOperation;
import appeng.tile.networking.TileCableBus;
import appeng.util.PatternMultiplierHelper;
import appeng.util.Platform;

public class ContainerWirelessDualInterfaceTerminal extends ContainerMonitor
    implements IContainerCraftingPacket, IWidgetPatternContainer, IConfigurableObject {

    public final ContainerInterfaceTerminal delegateContainer;
    private final PatternContainer patternPanel;

    @GuiSync(97)
    public boolean craftingMode = true;

    @GuiSync(96)
    public boolean substitute = false;

    @GuiSync(95)
    public boolean combine = false;

    @GuiSync(94)
    public boolean beSubstitute = false;

    @GuiSync(93)
    public boolean inverted;

    @GuiSync(92)
    public int activePage = 0;

    @GuiSync(91)
    public boolean prioritize = false;

    private final IPatternTerminal it;

    public ContainerWirelessDualInterfaceTerminal(InventoryPlayer ip, ITerminalHost monitorable) {
        super(ip, monitorable);
        this.patternPanel = new PatternContainer(ip, monitorable, this);
        this.delegateContainer = new ContainerInterfaceTerminal(ip, (IActionHost) monitorable);
        this.it = (IPatternTerminal) monitorable;
        this.setMonitor();
        this.lockSlot();
        this.bindPlayerInventory(ip, 14, 0);
    }

    private void lockSlot() {
        if (this.it instanceof WirelessTerminal wirelessTerminal) {
            this.lockPlayerInventorySlot(wirelessTerminal.getInventorySlot());
        }
    }

    @Override
    void setMonitor() {
        if (this.host instanceof INetworkTerminal) {
            final IGridNode node = ((IGridHost) this.host).getGridNode(ForgeDirection.UNKNOWN);
            if (node != null) {
                this.networkNode = node;
                final IGrid g = node.getGrid();
                if (g != null) {
                    this.setPowerSource(new ChannelPowerSrc(this.networkNode, g.getCache(IEnergyGrid.class)));
                    IStorageGrid storageGrid = g.getCache(IStorageGrid.class);
                    this.monitor.setMonitor(storageGrid.getItemInventory());
                    this.fluidMonitor.setMonitor(storageGrid.getFluidInventory(), storageGrid.getItemInventory());
                    this.monitor.setFluidMonitorObject(this.fluidMonitor);
                    if (this.monitor.getMonitor() == null) {
                        this.setValidContainer(false);
                    } else {
                        this.monitor.addListener();
                        this.fluidMonitor.addListener();
                        this.setCellInventory(this.monitor.getMonitor());
                    }
                }
            } else {
                this.setValidContainer(false);
            }
        }
    }

    @Override
    public void putStackInSlot(int slot, ItemStack item) {
        super.putStackInSlot(slot, item);
        this.patternPanel.getAndUpdateOutput();
    }

    @Override
    public void putStacksInSlots(final ItemStack[] par1ArrayOfItemStack) {
        super.putStacksInSlots(par1ArrayOfItemStack);
        this.patternPanel.getAndUpdateOutput();
    }

    public void detectAndSendChanges() {
        if (Platform.isClient()) {
            return;
        }
        this.patternPanel.detectAndSendChanges();
        super.detectAndSendChanges();
        this.delegateContainer.detectAndSendChanges();
    }

    @Override
    public void onSlotChange(final Slot s) {
        if (this.patternPanel != null) {
            this.patternPanel.onSlotChange(s);
        } else {
            AELog.warn("patternPanel is null!");
        }
        super.onSlotChange(s);
    }

    public void setInverted(boolean value) {
        this.inverted = value;
    }

    @Override
    public void onUpdate(String field, Object oldValue, Object newValue) {
        super.onUpdate(field, oldValue, newValue);
        this.patternPanel.onUpdate(field, oldValue, newValue);
    }

    @Override
    public void doAction(final EntityPlayerMP player, final InventoryAction action, final int slotId, final long id) {
        try {
            if (id >= 0) {
                delegateContainer.doAction(player, action, slotId, id);
            } else if (id == -1) {
                Slot s = this.inventorySlots.get(slotId);
                if (((s instanceof SlotPatternFake) || (s instanceof SlotFakeCraftingMatrix)
                    || (s instanceof SlotPatternTerm))) {
                    if (action == InventoryAction.MOVE_REGION) {
                        super.doAction(player, InventoryAction.MOVE_REGION, slotId, id);
                        return;
                    }
                    if (action == InventoryAction.PICKUP_SINGLE) {
                        super.doAction(player, InventoryAction.PICKUP_OR_SET_DOWN, slotId, id);
                        return;
                    }
                    Slot slot = getSlot(slotId);
                    ItemStack stack = player.inventory.getItemStack();
                    if (Util.getFluidFromItem(stack) == null || Util.getFluidFromItem(stack).amount <= 0
                        || this.isCraftingMode()) {
                        super.doAction(player, action, slotId, id);
                        return;
                    }
                    if (validPatternSlot(slot) && (stack.getItem() instanceof IFluidContainerItem
                        || FluidContainerRegistry.isContainer(stack))) {
                        FluidStack fluid = null;
                        switch (action) {
                            case PICKUP_OR_SET_DOWN -> {
                                fluid = Util.getFluidFromItem(stack);
                                slot.putStack(ItemFluidPacket.newStack(fluid));
                            }
                            case SPLIT_OR_PLACE_SINGLE -> {
                                fluid = Util.getFluidFromItem(Util.copyStackWithSize(stack, 1));
                                FluidStack origin = ItemFluidPacket.getFluidStack(slot.getStack());
                                if (fluid != null && fluid.equals(origin)) {
                                    fluid.amount += origin.amount;
                                    if (fluid.amount <= 0) fluid = null;
                                }
                                slot.putStack(ItemFluidPacket.newStack(fluid));
                            }
                        }
                        if (fluid == null) {
                            super.doAction(player, action, slotId, id);
                        }
                    }
                }
            } else if (id == -2) {
                super.doAction(player, action, slotId, id);
            }
        } catch (Exception e) {
            AELog.error(e);
        }
    }

    protected boolean validPatternSlot(Slot slot) {
        return slot instanceof SlotPatternFake || slot instanceof SlotPatternOutputs;
    }

    @Override
    public IGridNode getNetworkNode() {
        return this.it.getGridNode();
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
        return false;
    }

    @Override
    public ItemStack[] getViewCells() {
        return new ItemStack[0];
    }

    @Override
    public IPatternContainer getContainer() {
        return this.patternPanel;
    }

    public List<ICrafting> getCrafters() {
        return this.crafters;
    }

    public void doubleStacks(int value, NBTTagCompound tag) {
        ImmutablePair<World, IInterfaceViewable> result = getWorldAndHost(tag);
        if (result == null) return;
        doublePatterns(value, result.left, result.right);
    }

    private ImmutablePair<World, IInterfaceViewable> getWorldAndHost(NBTTagCompound tag) {
        Util.DimensionalCoordSide intMsg = Util.DimensionalCoordSide.readFromNBT(tag);
        World w = DimensionManager.getWorld(intMsg.getDimension());
        TileEntity tile = w.getTileEntity(intMsg.x, intMsg.y, intMsg.z);
        IInterfaceViewable host;
        if (tile instanceof TileCableBus) {
            host = (IInterfaceViewable) ((TileCableBus) tile).getPart(intMsg.getSide());
        } else if (tile instanceof IInterfaceViewable iv) {
            host = iv;
        } else if ((ModAndClassUtil.GT5 || ModAndClassUtil.GT5NH)) {
            host = GTUtil.getIInterfaceViewable(tile);
            if (host == null) return null;
        } else {
            return null;
        }
        return ImmutablePair.of(w, host);
    }

    private void doublePatterns(int val, World w, IInterfaceViewable host) {
        IInventory patterns = host.getPatterns();
        boolean fast = (val & 1) != 0;
        boolean backwards = (val & 2) != 0;
        CraftingGridCache.pauseRebuilds();
        try {
            for (int i = 0; i < patterns.getSizeInventory(); i++) {
                ItemStack stack = patterns.getStackInSlot(i);
                if (stack != null && stack.getItem() instanceof ICraftingPatternItem cpi) {
                    ICraftingPatternDetails details = cpi.getPatternForItem(stack, w);
                    if (details != null && !details.isCraftable()) {
                        int max = backwards ? PatternMultiplierHelper.getMaxBitDivider(details)
                            : PatternMultiplierHelper.getMaxBitMultiplier(details);
                        if (max > 0) {
                            ItemStack copy = stack.copy();
                            PatternMultiplierHelper
                                .applyModification(copy, (fast ? Math.min(3, max) : 1) * (backwards ? -1 : 1));
                            patterns.setInventorySlotContents(i, copy);
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}
        CraftingGridCache.unpauseRebuilds();
        this.sendToClient(host);
    }

    private void sendToClient(IInterfaceViewable host) {
        PacketInterfaceTerminalUpdate update = new PacketInterfaceTerminalUpdate();
        Map map = Ae2Reflect.getTracked(this.delegateContainer);
        Object o = map.get(host);
        if (o == null) return;
        try {
            Field f = o.getClass()
                .getDeclaredField("id");
            f.setAccessible(true);
            long id = (long) f.get(o);
            Method m = o.getClass()
                .getDeclaredMethod("updateNBT");
            m.setAccessible(true);
            m.invoke(o);
            Field f1 = o.getClass()
                .getDeclaredField("invNbt");
            f1.setAccessible(true);
            NBTTagList tag = (NBTTagList) f1.get(o);
            int[] size = new int[host.rowSize() * host.rows()];
            for (int i = 0; i < size.length; i++) {
                size[i] = i;
            }
            update.addOverwriteEntry(id)
                .setOnline(true)
                .setItems(size, tag);
            update.encode();
            NetworkHandler.instance.sendTo(update, (EntityPlayerMP) this.getPlayerInv().player);
        } catch (Exception ignored) {}
    }

    @Override
    public void addMESlotToContainer(AppEngSlot s) {
        this.addSlotToContainer(s);
    }

    public void setStick(NBTTagCompound tag) {
        Util.DimensionalCoordSide c = Util.DimensionalCoordSide.readFromNBT(tag);
        World w = DimensionManager.getWorld(c.getDimension());
        if (ModAndClassUtil.GT5 || ModAndClassUtil.GT5NH) {
            GTUtil.setDataStick(c.x, c.y, c.z, this.player, w);
        }
    }

    @Override
    public void processItemList() {
        super.processItemList();
        this.fluidMonitor.processItemList();
    }

    @Override
    public void removeCraftingFromCrafters(ICrafting c) {
        super.removeCraftingFromCrafters(c);
        this.fluidMonitor.removeCraftingFromCrafters(c);
    }

    @Override
    public void addCraftingToCrafters(ICrafting c) {
        super.addCraftingToCrafters(c);
        this.fluidMonitor.queueInventory(c);
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        if (this.fluidMonitor.getMonitor() != null) this.fluidMonitor.removeListener();
    }

    @Override
    public void saveChanges() {
        if (Platform.isServer()) {
            this.it.saveSettings();
        }
    }

    @Override
    public ItemStack slotClick(int slotId, int clickedButton, int mode, EntityPlayer player) {
        if (slotId == -999) return this.getPlayerInv()
            .getItemStack();
        return super.slotClick(slotId, clickedButton, mode, player);
    }

    @Override
    public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removedStack,
        ItemStack newStack) {

    }

    public void setCraftingMode(final boolean craftingMode) {
        this.craftingMode = craftingMode;
    }

    public boolean isCraftingMode() {
        return this.craftingMode;
    }

    public void setCrafting(boolean craftingMode) {
        this.craftingMode = craftingMode;
        this.it.setCraftingRecipe(craftingMode);
    }

    public void setModifier(int val, NBTTagCompound tag) {
        ImmutablePair<World, IInterfaceViewable> result = this.getWorldAndHost(tag);
        if (result == null) return;
        ItemStack currentItem = this.player.inventory.getItemStack();
        if (currentItem != null && currentItem.getItem() instanceof ItemPatternModifier) {
            int slot = val >> 2;
            boolean shift = (val & 1) != 0;
            boolean backwards = (val & 2) != 0;
            if (!backwards) {
                // inject all item to pattern modifier
                injectPatternToPatternModifier(result.right, slot, shift);
            } else {
                extractPatternToInterface(result.right);
            }
            this.sendToClient(result.right);
        }
    }

    private void extractPatternToInterface(IInterfaceViewable host) {
        PatternModifierInventory patternModifierInventory = new PatternModifierInventory(
            this.player.inventory.getItemStack(),
            -1,
            player);
        patternModifierInventory.extractToHost(host);
    }

    private void injectPatternToPatternModifier(IInterfaceViewable host, int slot, boolean shift) {
        IInventory patterns = host.getPatterns();
        PatternModifierInventory patternModifierInventory = new PatternModifierInventory(
            this.player.inventory.getItemStack(),
            -1,
            player);
        if (shift) {
            for (int i = 0; i < patterns.getSizeInventory(); i++) {
                ItemStack pattern = patterns.getStackInSlot(i);
                if (patternModifierInventory.injectItems(pattern)) {
                    patterns.setInventorySlotContents(i, null);
                } else {
                    break;
                }
            }
        } else {
            if (patternModifierInventory.injectItems(patterns.getStackInSlot(slot))) {
                patterns.setInventorySlotContents(slot, null);
            }
        }

    }

}
