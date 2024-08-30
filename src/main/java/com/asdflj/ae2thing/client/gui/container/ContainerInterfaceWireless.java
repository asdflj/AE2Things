package com.asdflj.ae2thing.client.gui.container;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

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
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

import com.asdflj.ae2thing.client.gui.container.slot.SlotPatternFake;
import com.asdflj.ae2thing.client.gui.container.widget.IWidgetPatternContainer;
import com.asdflj.ae2thing.client.gui.container.widget.PatternContainer;
import com.asdflj.ae2thing.inventory.IPatternTerminal;
import com.asdflj.ae2thing.inventory.item.WirelessInterfaceTerminalInventory;
import com.asdflj.ae2thing.util.Ae2Reflect;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.util.Util;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.security.IActionHost;
import appeng.api.storage.ITerminalHost;
import appeng.container.guisync.GuiSync;
import appeng.container.implementations.ContainerInterfaceTerminal;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInterfaceTerminalUpdate;
import appeng.helpers.IContainerCraftingPacket;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.InventoryAction;
import appeng.me.cache.CraftingGridCache;
import appeng.tile.networking.TileCableBus;
import appeng.util.PatternMultiplierHelper;
import appeng.util.Platform;

public class ContainerInterfaceWireless extends BaseNetworkContainer
    implements IContainerCraftingPacket, IWidgetPatternContainer {

    public final ContainerInterfaceTerminal delegateContainer;
    private final PatternContainer patternPanel;
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

    public ContainerInterfaceWireless(InventoryPlayer ip, ITerminalHost monitorable) {
        super(ip, monitorable);
        this.patternPanel = new PatternContainer(ip, monitorable, this);
        this.delegateContainer = new ContainerInterfaceTerminal(ip, (IActionHost) monitorable);
        this.it = (IPatternTerminal) monitorable;
        if (monitorable instanceof WirelessInterfaceTerminalInventory witi) {
            this.setPowerSource(witi);
            this.setCellInventory(null);
        }
        this.bindPlayerInventory(ip, 14, 0);
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
        this.patternPanel.onSlotChange(s);
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
        if (id != -1) {
            delegateContainer.doAction(player, action, slotId, id);
        } else {
            Slot s = this.inventorySlots.get(slotId);
            if (s instanceof SlotPatternFake) {
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
                if (Util.getFluidFromItem(stack) == null || Util.getFluidFromItem(stack).amount <= 0) {
                    super.doAction(player, action, slotId, id);
                    return;
                }
                if (validPatternSlot(slot)
                    && (stack.getItem() instanceof IFluidContainerItem || FluidContainerRegistry.isContainer(stack))) {
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
        }
    }

    protected boolean validPatternSlot(Slot slot) {
        return slot instanceof SlotPatternFake;
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
        Util.DimensionalCoordSide intMsg = Util.DimensionalCoordSide.readFromNBT(tag);
        World w = DimensionManager.getWorld(intMsg.getDimension());
        TileEntity tile = w.getTileEntity(intMsg.x, intMsg.y, intMsg.z);
        IInterfaceHost host;
        if (tile instanceof TileCableBus) {
            host = (IInterfaceHost) ((TileCableBus) tile).getPart(intMsg.getSide());
        } else if (tile instanceof IInterfaceHost ih) {
            host = ih;
        } else {
            return;
        }
        doublePatterns(value, w, host);
    }

    private void doublePatterns(int val, World w, IInterfaceHost host) {
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

    private void sendToClient(IInterfaceHost host) {
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
}
