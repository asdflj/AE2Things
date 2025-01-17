package com.asdflj.ae2thing.proxy;

import static codechicken.lib.gui.GuiDraw.getMousePosition;

import java.awt.Point;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;

import com.asdflj.ae2thing.client.gui.BaseMEGui;
import com.asdflj.ae2thing.client.gui.GuiMonitor;
import com.asdflj.ae2thing.client.render.BlockPosHighlighter;
import com.asdflj.ae2thing.common.Config;
import com.asdflj.ae2thing.common.item.ItemPhial;
import com.asdflj.ae2thing.loader.KeybindLoader;
import com.asdflj.ae2thing.loader.ListenerLoader;
import com.asdflj.ae2thing.loader.RenderLoader;
import com.asdflj.ae2thing.nei.recipes.DefaultExtractorLoader;
import com.asdflj.ae2thing.util.FindITUtil;
import com.asdflj.ae2thing.util.ModAndClassUtil;

import codechicken.nei.LayoutManager;
import codechicken.nei.api.API;
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
            if (ModAndClassUtil.THE) {
                ItemPhial.getItems()
                    .forEach(API::hideItem);
            }
            if (ModAndClassUtil.FIND_IT) {
                FindITUtil.instance.run();
            }
        }
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        (new ListenerLoader()).run();
        (new RenderLoader()).run();
        (new KeybindLoader()).run();
        MinecraftForge.EVENT_BUS.register(new BlockPosHighlighter());
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
            if (Minecraft.getMinecraft().currentScreen instanceof GuiMonitor gim) {
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

    @SubscribeEvent
    public void initGuiEvent(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.gui instanceof BaseMEGui bg) {
            bg.initDone();
        }
    }

    @SubscribeEvent
    public void onClientPostTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        // `WorldClient` is only available on the client-side, thus effectively checking if the game is running on
        // the client. We are only interested in highlighting slots when the player is in a GUI; the operation is
        // bound client-side.
        if (Minecraft.getMinecraft().theWorld == null) {
            return;
        }

        // We are only interested in GUIs that contain some kind of inventory.
        final GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (!(screen instanceof GuiContainer)) {
            return;
        }
        if (ModAndClassUtil.FIND_IT) {
            FindITUtil.instance.highlighter();
        }
    }
}
