package com.asdflj.ae2thing.crossmod.waila;

import static codechicken.lib.gui.GuiDraw.TOOLTIP_HANDLER;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.client.render.RenderHelper;
import com.asdflj.ae2thing.common.item.ItemBackpackTerminal;
import com.asdflj.ae2thing.common.item.ItemInfinityStorageCell;
import com.asdflj.ae2thing.common.item.ItemInfinityStorageFluidCell;

import appeng.api.AEApi;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import codechicken.lib.gui.GuiDraw;

public class CellContentHandler extends mcp.mobius.waila.handlers.nei.TooltipHandlerWaila {

    public static HashSet<Class<? extends Item>> blackList = new HashSet<>();
    private static final List<IAEStack<?>> cellContent = new ArrayList<>();
    private static final int limit = 5;
    private static final GuiDraw.ITooltipLineHandler tooltipLineHandler = new GuiDraw.ITooltipLineHandler() {

        @Override
        public Dimension getSize() {
            return new Dimension(100, 20);
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

    @Override
    public List<String> handleItemTooltip(GuiContainer arg0, ItemStack itemstack, int x, int y,
        List<String> currentToolTip) {
        if (itemstack != null && AEApi.instance()
            .registries()
            .cell()
            .isCellHandled(itemstack)
            && currentToolTip.size() > 2
            && (itemstack.getItem() != null && !blackList.contains(
                itemstack.getItem()
                    .getClass()))) {
            try {
                cellContent.clear();
                IMEInventoryHandler handler;
                handler = AEApi.instance()
                    .registries()
                    .cell()
                    .getCellInventory(itemstack, null, StorageChannel.ITEMS);
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
                    addTooltip(list, currentToolTip);
                    return currentToolTip;
                }
                handler = AEApi.instance()
                    .registries()
                    .cell()
                    .getCellInventory(itemstack, null, StorageChannel.FLUIDS);
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
                    addTooltip(list, currentToolTip);
                    return currentToolTip;
                }
            } catch (Exception ignored) {}
        }
        return currentToolTip;
    }

    private void addTooltip(List<IAEStack<?>> list, List<String> currentToolTip) {
        if (!list.isEmpty()) {
            cellContent.addAll(list);
            int id = getTipLineId(tooltipLineHandler);
            currentToolTip.add(currentToolTip.size() - 1, TOOLTIP_HANDLER + id);
        }
    }

    static {
        blackList.add(ItemBackpackTerminal.class);
        blackList.add(ItemInfinityStorageCell.class);
        blackList.add(ItemInfinityStorageFluidCell.class);
    }
}
