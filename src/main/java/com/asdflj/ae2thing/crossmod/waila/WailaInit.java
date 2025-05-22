package com.asdflj.ae2thing.crossmod.waila;

import com.asdflj.ae2thing.util.Util;

import appeng.util.Platform;
import codechicken.nei.guihook.GuiContainerManager;
import cpw.mods.fml.common.event.FMLInterModComms;
import mcp.mobius.waila.api.IWailaRegistrar;

public class WailaInit {

    public static void run() {
        FMLInterModComms.sendMessage("Waila", "register", WailaInit.class.getName() + ".register");
    }

    public static void register(final IWailaRegistrar registrar) {
        if (Platform.isClient()) {
            GuiContainerManager.addTooltipHandler(new TooltipHandlerWaila());
            GuiContainerManager.addTooltipHandler(new CellContentHandler());
            GuiContainerManager.addTooltipHandler(new CraftingStatePreview());
            GuiContainerManager.addTooltipHandler(new PatternPermutationToolTip());
            if (Util.getAEVersion() < 555) {
                GuiContainerManager.addTooltipHandler(new EncodedPattern());
            }

        }

    }
}
