package com.asdflj.ae2thing.crossmod.waila;

import static codechicken.lib.gui.GuiDraw.TOOLTIP_HANDLER;
import static codechicken.lib.gui.GuiDraw.getTipLineId;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.api.InventoryActionExtend;
import com.asdflj.ae2thing.client.render.RenderHelper;
import com.asdflj.ae2thing.network.CPacketInventoryActionExtend;
import com.asdflj.ae2thing.util.CPUCraftingPreview;

import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;
import codechicken.lib.gui.GuiDraw;

public class CraftingStatePreviewWaila extends mcp.mobius.waila.handlers.nei.TooltipHandlerWaila {

    private static int id = 0;
    private static final List<CPUCraftingPreview> cpus = new ArrayList<>();
    private static final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
    private static int width;
    private static final GuiDraw.ITooltipLineHandler tooltipLineHandler = new GuiDraw.ITooltipLineHandler() {

        private static final int HEIGHT = 30;

        @Override
        public Dimension getSize() {
            return new Dimension(width, HEIGHT * cpus.size());
        }

        @Override
        public void draw(int x, int y) {
            if (!cpus.isEmpty()) {
                int j = 0;
                for (CPUCraftingPreview cpu : cpus) {
                    fontRenderer.drawStringWithShadow(cpu.name, x, y + j * HEIGHT, 0xffffff);
                    for (int i = 0; i < cpu.itemList.size() && i < CPUCraftingPreview.maxSize; i++) {
                        IAEItemStack item = cpu.itemList.get(i);
                        RenderHelper
                            .renderAEStack(item, x + (i * 18), y + (fontRenderer.FONT_HEIGHT) + j * HEIGHT, 500f);
                    }
                    j++;
                }
            }
        }
    };

    @Override
    public List<String> handleItemTooltip(GuiContainer gui, ItemStack itemstack, int x, int y,
        List<String> currentToolTip) {
        if (AE2ThingAPI.instance()
            .terminal()
            .isPinTerminal(gui)) {
            IAEItemStack item = AEItemStack.create(itemstack);
            if (item == null) return currentToolTip;
            if (AE2ThingAPI.instance()
                .getPinned()
                .getPinnedItems()
                .contains(item)) {
                if (Minecraft.getMinecraft().thePlayer.ticksExisted % 20 == 0) {
                    AE2Thing.proxy.netHandler.sendToServer(
                        new CPacketInventoryActionExtend(InventoryActionExtend.GET_CRAFTING_STATE, 0, 0, item));
                }
                if (!cpus.isEmpty()) {
                    id = getTipLineId(tooltipLineHandler);
                    currentToolTip.add(currentToolTip.size() - 1, TOOLTIP_HANDLER + id);
                }
            }
        }

        return currentToolTip;
    }

    public static void readFromNBT(NBTTagCompound nbt) {
        cpus.clear();
        int size = 0;
        NBTTagList list = nbt.getTagList(Constants.CPU_LIST, 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound data = list.getCompoundTagAt(i);
            CPUCraftingPreview cpu = CPUCraftingPreview.readFromNBT(data);
            size = Math.max(cpu.itemList.size(), size);
            cpus.add(cpu);
        }
        width = size * 20;
    }
}
