package com.asdflj.ae2thing.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import org.jetbrains.annotations.NotNull;

import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.client.render.BlockPosHighlighter;
import com.gtnh.findit.FindIt;
import com.gtnh.findit.FindItConfig;
import com.gtnh.findit.FindItNetwork;
import com.gtnh.findit.fx.SlotHighlighter;
import com.gtnh.findit.service.itemfinder.FindItemRequest;
import com.gtnh.findit.util.AbstractStackFinder;

import appeng.api.util.DimensionalCoord;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotRestrictedInput;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.guihook.IContainerDrawHandler;
import codechicken.nei.guihook.IContainerInputHandler;

public class FindITUtil implements Runnable {

    private static SlotHighlighter slotHighlighter;
    private List<CellPos> cellPosList = new ArrayList<CellPos>();
    public static FindITUtil instance = new FindITUtil();
    private long expirationTime = 0;

    @Override
    public void run() {
        for (IContainerInputHandler handler : GuiContainerManager.inputHandlers) {
            if (handler instanceof AbstractStackFinder && handler.getClass()
                .getName()
                .contains("ClientItemFindService")) {
                GuiContainerManager.inputHandlers.remove(handler);
                GuiContainerManager.inputHandlers.add(new ItemFindService());
                break;
            }
        }
        for (IContainerDrawHandler handler : GuiContainerManager.drawHandlers) {
            if (handler instanceof SlotHighlighter s) {
                slotHighlighter = s;
                break;
            }
        }
    }

    public void setSlotHighlighter(List<CellPos> cellPosList, boolean closeGui) {
        this.cellPosList.clear();
        this.cellPosList.addAll(cellPosList);
        this.expirationTime = System.currentTimeMillis() + FindItConfig.ITEM_HIGHLIGHTING_DURATION * 1000L;
        List<DimensionalCoord> list = cellPosList.stream()
            .map(CellPos::getCoord)
            .collect(Collectors.toList());
        BlockPosHighlighter.highlightBlocks(
            Minecraft.getMinecraft().thePlayer,
            list,
            NameConst.NEI_FIND_CELL_ITEM_HIGHLIGHT,
            NameConst.NEI_FIND_CELL_ITEM_IN_OTHER_DIM);
        if (Minecraft.getMinecraft().currentScreen != null && closeGui) {
            Minecraft.getMinecraft().thePlayer.closeScreen();
        }

    }

    public void highlighter() {
        Container container = Minecraft.getMinecraft().thePlayer.openContainer;
        final GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (!(screen instanceof GuiContainer guiContainer)) {
            return;
        }
        if (!(container instanceof AEBaseContainer c)) return;
        if (System.currentTimeMillis() > expirationTime) {
            this.cellPosList.clear();
        }
        if (this.cellPosList.isEmpty()) {
            return;
        }
        if (c.getTarget() instanceof TileEntity t) {
            HashSet<Slot> slots = getSlots(c, t);
            slotHighlighter.highlightSlots(guiContainer, slots, FindItConfig.ITEM_HIGHLIGHTING_COLOR);
        }
    }

    private @NotNull HashSet<Slot> getSlots(AEBaseContainer c, TileEntity t) {
        HashSet<Slot> slots = new HashSet<>();
        for (CellPos cellPos : cellPosList) {
            if (cellPos.getCoord()
                .equals(new DimensionalCoord(t))) {
                for (int i = 0; i < c.inventorySlots.size(); i++) {
                    Slot s = c.inventorySlots.get(i);
                    if (s instanceof SlotRestrictedInput si && si.getSlotIndex() == cellPos.getSlot()) {
                        slots.add(s);
                    }
                }
            }
        }
        return slots;
    }

    private static class ItemFindService extends AbstractStackFinder {

        @Override
        protected String getKeyBindId() {
            return FindIt.isExtraUtilitiesLoaded() ? "gui.xu_ping" : "gui.findit.find_item";
        }

        @Override
        protected boolean findStack(ItemStack stack) {
            if (Minecraft.getMinecraft().thePlayer.openContainer instanceof AEBaseContainer) {
                AE2ThingAPI.instance()
                    .findCellItem(stack);
            } else {
                FindItNetwork.CHANNEL.sendToServer(new FindItemRequest(stack));
            }

            return true;
        }
    }

}
