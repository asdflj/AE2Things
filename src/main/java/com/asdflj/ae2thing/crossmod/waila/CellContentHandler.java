package com.asdflj.ae2thing.crossmod.waila;

import static codechicken.lib.gui.GuiDraw.TOOLTIP_HANDLER;
import static codechicken.lib.gui.GuiDraw.fontRenderer;
import static codechicken.lib.gui.GuiDraw.getTipLineId;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.client.render.RenderHelper;
import com.asdflj.ae2thing.common.item.ItemBackpackTerminal;
import com.asdflj.ae2thing.common.item.ItemInfinityStorageCell;
import com.asdflj.ae2thing.common.item.ItemInfinityStorageFluidCell;
import com.asdflj.ae2thing.util.NameConst;

import appeng.api.AEApi;
import appeng.api.storage.ICellWorkbenchItem;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.util.item.AEItemStack;
import codechicken.lib.gui.GuiDraw;

public class CellContentHandler extends mcp.mobius.waila.handlers.nei.TooltipHandlerWaila {

    public static HashSet<Class<? extends Item>> blackList = new HashSet<>();
    private static final List<IAEStack<?>> cellContent = new ArrayList<>();
    private static final List<IAEItemStack> upgradeCard = new ArrayList<>();
    private static final int limit = 5;
    private static final int width = 100;
    private static final int height = 20;
    private static final GuiDraw.ITooltipLineHandler cellItemStackHandler = new GuiDraw.ITooltipLineHandler() {

        @Override
        public Dimension getSize() {
            return new Dimension(width, height);
        }

        @Override
        public void draw(int x, int y) {
            if (!cellContent.isEmpty()) {
                for (int i = 0; i < cellContent.size() && i < limit; i++) {
                    IAEStack<?> item = cellContent.get(i);
                    RenderHelper.renderAEStack(item, x + (i * 18), y, 500f);
                }
                if (cellContent.size() > limit) {
                    Minecraft.getMinecraft().fontRenderer
                        .drawStringWithShadow("...", x + (limit * 18) + 1, y + 2, 0xffffff);
                }
            }
        }
    };
    private static final GuiDraw.ITooltipLineHandler cellUpgradeCardHandler = new GuiDraw.ITooltipLineHandler() {

        @Override
        public Dimension getSize() {
            return new Dimension(width, height + fontRenderer.FONT_HEIGHT);
        }

        @Override
        public void draw(int x, int y) {
            if (!upgradeCard.isEmpty()) {
                Minecraft.getMinecraft().fontRenderer
                    .drawStringWithShadow(I18n.format(NameConst.TT_INSTALLED_CARD), x, y, 0xA8A8A8);
                for (int i = 0; i < upgradeCard.size() && i < limit; i++) {
                    IAEStack<?> item = upgradeCard.get(i);
                    RenderHelper.renderAEStack(item, x + (i * 18), y + fontRenderer.FONT_HEIGHT, 500f, false);
                }
            }
        }
    };

    @Override
    public List<String> handleItemTooltip(GuiContainer arg0, ItemStack cell, int x, int y,
        List<String> currentToolTip) {
        if (cell != null && AEApi.instance()
            .registries()
            .cell()
            .isCellHandled(cell)
            && currentToolTip.size() >= 2
            && (cell.getItem() != null && !blackList.contains(
                cell.getItem()
                    .getClass()))) {
            try {
                cellContent.clear();
                upgradeCard.clear();
                IMEInventoryHandler handler;
                handler = AEApi.instance()
                    .registries()
                    .cell()
                    .getCellInventory(cell, null, StorageChannel.ITEMS);
                if (handler != null) {
                    IItemList<IAEItemStack> itemList = handler.getAvailableItems(
                        AEApi.instance()
                            .storage()
                            .createPrimitiveItemList(),
                        0);
                    List<IAEStack<?>> list = Arrays.stream(itemList.toArray(new IAEItemStack[0]))
                        .sorted(
                            Comparator.comparingLong(IAEItemStack::getStackSize)
                                .reversed())
                        .collect(Collectors.toList());
                    addTooltip(list, cell, currentToolTip);
                    return currentToolTip;
                }
                handler = AEApi.instance()
                    .registries()
                    .cell()
                    .getCellInventory(cell, null, StorageChannel.FLUIDS);
                if (handler != null) {
                    IItemList<IAEFluidStack> itemList = handler.getAvailableItems(
                        AEApi.instance()
                            .storage()
                            .createFluidList(),
                        0);
                    List<IAEStack<?>> list = Arrays.stream(itemList.toArray(new IAEFluidStack[0]))
                        .sorted(
                            Comparator.comparingLong(IAEFluidStack::getStackSize)
                                .reversed())
                        .collect(Collectors.toList());
                    addTooltip(list, cell, currentToolTip);
                    return currentToolTip;
                }
            } catch (Exception ignored) {}
        }
        return currentToolTip;
    }

    private void addTooltip(List<IAEStack<?>> list, ItemStack cell, List<String> currentToolTip) {
        if (!list.isEmpty()) {
            cellContent.addAll(list);
            currentToolTip.add(currentToolTip.size() - 1, TOOLTIP_HANDLER + getTipLineId(cellItemStackHandler));
        }
        addUpgradeCard(cell, currentToolTip);
    }

    private void addUpgradeCard(ItemStack cell, List<String> currentToolTip) {
        if (cell != null && cell.getItem() != null && cell.getItem() instanceof ICellWorkbenchItem workbenchItem) {
            IInventory inv = workbenchItem.getUpgradesInventory(cell);
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                ItemStack card = inv.getStackInSlot(i);
                if (card != null) {
                    upgradeCard.add(AEItemStack.create(card));
                }
            }
            if (!upgradeCard.isEmpty()) {
                currentToolTip.add(currentToolTip.size() - 1, TOOLTIP_HANDLER + getTipLineId(cellUpgradeCardHandler));
            }

        }
    }

    static {
        blackList.add(ItemBackpackTerminal.class);
        blackList.add(ItemInfinityStorageCell.class);
        blackList.add(ItemInfinityStorageFluidCell.class);
    }
}
