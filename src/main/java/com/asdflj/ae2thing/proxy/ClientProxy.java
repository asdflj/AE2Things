package com.asdflj.ae2thing.proxy;

import static net.minecraft.client.gui.GuiScreen.isShiftKeyDown;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.p455w0rd.wirelesscraftingterminal.client.gui.GuiWirelessCraftingTerminal;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.api.MouseWheelHandler;
import com.asdflj.ae2thing.api.adapter.terminal.item.BackpackTerminal;
import com.asdflj.ae2thing.api.adapter.terminal.item.DualInterfaceTerminal;
import com.asdflj.ae2thing.api.adapter.terminal.item.FCBaseItemTerminal;
import com.asdflj.ae2thing.api.adapter.terminal.item.FCUltraTerminal;
import com.asdflj.ae2thing.api.adapter.terminal.item.WCTWirelessCraftingTerminal;
import com.asdflj.ae2thing.api.adapter.terminal.parts.AETerminal;
import com.asdflj.ae2thing.api.adapter.terminal.parts.FCPatternTerminal;
import com.asdflj.ae2thing.client.event.CraftTracking;
import com.asdflj.ae2thing.client.event.GuiOverlayButtonEvent;
import com.asdflj.ae2thing.client.event.NotificationEvent;
import com.asdflj.ae2thing.client.event.OpenTerminalEvent;
import com.asdflj.ae2thing.client.event.UpdateAmountTextEvent;
import com.asdflj.ae2thing.client.gui.BaseMEGui;
import com.asdflj.ae2thing.client.gui.GuiCraftingTerminal;
import com.asdflj.ae2thing.client.gui.GuiInfusionPatternTerminal;
import com.asdflj.ae2thing.client.gui.GuiWirelessDualInterfaceTerminal;
import com.asdflj.ae2thing.client.render.BlockPosHighlighter;
import com.asdflj.ae2thing.client.render.Notification;
import com.asdflj.ae2thing.common.item.ItemPhial;
import com.asdflj.ae2thing.loader.KeybindLoader;
import com.asdflj.ae2thing.loader.ListenerLoader;
import com.asdflj.ae2thing.loader.RenderLoader;
import com.asdflj.ae2thing.nei.recipes.DefaultExtractorLoader;
import com.asdflj.ae2thing.network.CPacketCraftRequest;
import com.asdflj.ae2thing.util.FindITUtil;
import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.glodblock.github.client.gui.GuiFluidCraftingWireless;
import com.glodblock.github.client.gui.GuiFluidPatternExWireless;
import com.glodblock.github.client.gui.GuiFluidPatternTerminal;
import com.glodblock.github.client.gui.GuiFluidPatternTerminalEx;
import com.glodblock.github.client.gui.GuiFluidPatternWireless;

import appeng.api.events.GuiScrollEvent;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.client.gui.implementations.GuiCraftingTerm;
import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.client.gui.implementations.GuiPatternTerm;
import appeng.client.gui.implementations.GuiPatternTermEx;
import appeng.client.gui.implementations.GuiWirelessTerm;
import codechicken.nei.api.API;
import codechicken.nei.recipe.GuiOverlayButton;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.GuiRecipeButton;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;

public class ClientProxy extends CommonProxy {

    private static GuiOverlayButton overlayButton = null;
    public static List<MouseWheelHandler> mouseHandlers = new ArrayList<>();

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

    public static GuiOverlayButton getOverlayButton() {
        return overlayButton;
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
    }

    @SubscribeEvent
    public void trackingMissingItems(CraftTracking c) {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        IItemList<IAEItemStack> list = c.getItems();
        if (!list.isEmpty() && AE2ThingAPI.instance()
            .terminal()
            .isCraftingTerminal(screen)) {
            for (IAEItemStack is : list) {
                AE2Thing.proxy.netHandler.sendToServer(new CPacketCraftRequest(is, isShiftKeyDown()));
                is.reset();
                break;
            }
        }
    }

    @SubscribeEvent
    public void updateCraftAmount(UpdateAmountTextEvent amount) {
        amount.updateAmount();
    }

    @SubscribeEvent
    public boolean handleMouseWheelInput(GuiScrollEvent event) {
        if (mouseHandlers.isEmpty()) return false;
        for (MouseWheelHandler handler : mouseHandlers) {
            if (handler.handleMouseWheel(event, overlayButton)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        (new ListenerLoader()).run();
        (new RenderLoader()).run();
        (new KeybindLoader()).run();
        MinecraftForge.EVENT_BUS.register(new BlockPosHighlighter());
        AE2ThingAPI.instance()
            .terminal()
            .registerTerminal(GuiMEMonitorable.class);
        AE2ThingAPI.instance()
            .terminal()
            .registerTerminal(GuiCraftingTerm.class);
        AE2ThingAPI.instance()
            .terminal()
            .registerTerminal(GuiPatternTerm.class);
        AE2ThingAPI.instance()
            .terminal()
            .registerTerminal(GuiPatternTermEx.class);
        AE2ThingAPI.instance()
            .terminal()
            .registerTerminal(GuiFluidPatternTerminalEx.class);
        AE2ThingAPI.instance()
            .terminal()
            .registerTerminal(GuiFluidPatternTerminal.class);
        AE2ThingAPI.instance()
            .terminal()
            .registerTerminal(GuiFluidPatternExWireless.class);
        AE2ThingAPI.instance()
            .terminal()
            .registerTerminal(GuiFluidPatternWireless.class);
        AE2ThingAPI.instance()
            .terminal()
            .registerTerminal(GuiFluidCraftingWireless.class);
        AE2ThingAPI.instance()
            .terminal()
            .registerTerminal(GuiWirelessTerm.class);
        if (ModAndClassUtil.WCT) {
            AE2ThingAPI.instance()
                .terminal()
                .registerTerminal(GuiWirelessCraftingTerminal.class);
        }
        if (ModAndClassUtil.THE) {
            AE2ThingAPI.instance()
                .terminal()
                .registerTerminal(GuiInfusionPatternTerminal.class);
        }

        AE2ThingAPI.instance()
            .terminal()
            .registerTerminalBlackList(GuiCraftingTerminal.class);
        AE2ThingAPI.instance()
            .terminal()
            .registerTerminalBlackList(GuiWirelessDualInterfaceTerminal.class);
        AE2ThingAPI.instance()
            .terminal()
            .registerTerminalSet(BackpackTerminal.instance);
        AE2ThingAPI.instance()
            .terminal()
            .registerTerminalSet(DualInterfaceTerminal.instance);
        AE2ThingAPI.instance()
            .terminal()
            .registerTerminalSet(FCBaseItemTerminal.instance);
        AE2ThingAPI.instance()
            .terminal()
            .registerTerminalSet(FCUltraTerminal.instance);
        AE2ThingAPI.instance()
            .terminal()
            .registerTerminalSet(WCTWirelessCraftingTerminal.instance);
        AE2ThingAPI.instance()
            .terminal()
            .registerTerminalSet(new FCPatternTerminal());
        AE2ThingAPI.instance()
            .terminal()
            .registerTerminalSet(new AETerminal());
    }

    @SubscribeEvent
    public void tickEvent(TickEvent.PlayerTickEvent event) {
        AE2ThingAPI.instance()
            .getPinned()
            .updateCraftingItems();
    }

    @SubscribeEvent
    public void onActionPerformedEventPost(GuiRecipeButton.UpdateRecipeButtonsEvent.Post event) {
        if (!(event.gui instanceof GuiRecipe<?>)) return;
        overlayButton = null;
        for (GuiRecipeButton btn : event.buttonList) {
            if (btn instanceof GuiOverlayButton gob) {
                gob.setRequireShiftForOverlayRecipe(false);
            }
        }
    }

    @SubscribeEvent
    public void onActionOverlayButton(GuiOverlayButtonEvent event) {
        overlayButton = event.getButton();
    }

    @SubscribeEvent
    public void initGuiEvent(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.gui instanceof BaseMEGui bg) {
            bg.initDone();
        }
        if (AE2ThingAPI.instance()
            .terminal()
            .isCraftingTerminal(event.gui)) {
            MinecraftForge.EVENT_BUS.post(new CraftTracking());
        }
        if (UpdateAmountTextEvent.needUpdateAmountText()) {
            MinecraftForge.EVENT_BUS.post(new UpdateAmountTextEvent());
        }
    }

    @SubscribeEvent
    public void initGuiEvent(GuiScreenEvent.InitGuiEvent.Pre event) {
        if (AE2ThingAPI.instance()
            .terminal()
            .isPinTerminal(event.gui)) {
            AE2ThingAPI.instance()
                .getPinned()
                .prune();
        }
    }

    @SubscribeEvent
    public void notificationEvent(NotificationEvent event) {
        Notification.INSTANCE.add(event);
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type == RenderGameOverlayEvent.ElementType.ALL) {
            Notification.INSTANCE.draw();
        }
    }

    @SubscribeEvent
    public void openTerminalEvent(OpenTerminalEvent event) {
        event.openTerminal();
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

    @SubscribeEvent
    public void ClientDisconnectionFromServerEvent(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        AE2ThingAPI.instance()
            .getPinned()
            .clear();
        Notification.INSTANCE.clear();
    }
}
