package com.asdflj.ae2thing.proxy;

import static codechicken.lib.gui.GuiDraw.getMousePosition;

import java.awt.Point;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.client.gui.GuiItemMonitor;
import com.asdflj.ae2thing.common.Config;
import com.asdflj.ae2thing.loader.KeybindLoader;
import com.asdflj.ae2thing.loader.RenderLoader;
import com.asdflj.ae2thing.nei.recipes.DefaultExtractorLoader;
import com.asdflj.ae2thing.util.ModAndClassUtil;

import codechicken.nei.LayoutManager;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class ClientProxy extends CommonProxy {

    private final ItemStack[] hoveredStack = new ItemStack[2];
    private static long refreshTick = System.currentTimeMillis();

    // Override CommonProxy methods here, if you want a different behaviour on the client (e.g. registering renders).
    // Don't forget to call the super methods as well.
    @Override
    public void onLoadComplete(FMLLoadCompleteEvent event) {
        super.onLoadComplete(event);
        if (ModAndClassUtil.NEI) {
            new DefaultExtractorLoader().run();
        }
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        (new RenderLoader()).run();
        (new KeybindLoader()).run();
    }

    private ItemStack getStackMouseOver(GuiContainer window) {
        try {
            Point mousePos = getMousePosition();
            ItemStack item = LayoutManager.instance()
                .getStackUnderMouse(window, mousePos.x, mousePos.y);
            if (item != null) return item;
        } catch (Exception ignored) {

        }
        return null;
    }

    @SubscribeEvent
    public void tickEvent(TickEvent.PlayerTickEvent event) {
        if (Config.backPackTerminalFillItemName && refreshTick + 500 < System.currentTimeMillis()
            && ModAndClassUtil.NEI) {
            if (Minecraft.getMinecraft().currentScreen instanceof GuiItemMonitor gim) {
                refreshTick = System.currentTimeMillis();
                hoveredStack[1] = this.getStackMouseOver(gim);
                if (hoveredStack[1] == null) return;
                if (hoveredStack[0] != hoveredStack[1]) {
                    hoveredStack[0] = hoveredStack[1];
                } else {
                    gim.setSearchString(hoveredStack[0].getDisplayName(), true, 0);
                }
            }
        }

    }
}
