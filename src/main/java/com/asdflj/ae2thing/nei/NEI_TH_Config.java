package com.asdflj.ae2thing.nei;

import java.util.ArrayList;
import java.util.List;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.Tags;
import com.asdflj.ae2thing.client.gui.GuiCraftingTerminal;
import com.asdflj.ae2thing.client.gui.GuiInfusionPatternTerminal;
import com.asdflj.ae2thing.client.gui.GuiWirelessDualInterfaceTerminal;
import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.glodblock.github.nei.recipes.FluidRecipe;

import codechicken.lib.config.ConfigTagParent;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;

@SuppressWarnings("unused")
public class NEI_TH_Config implements IConfigureNEI {

    private static final ConfigTagParent tag = NEIClientConfig.global.config;

    @Override
    public void loadConfig() {
        API.registerNEIGuiHandler(new AE2TH_NEIGuiHandler());
        List<String> recipes = new ArrayList<>();
        recipes.add("crafting");
        recipes.add("crafting2x2");
        for (String identifier : recipes) {
            // that NEE handlers take priority
            if (!API.hasGuiOverlayHandler(GuiCraftingTerminal.class, identifier)) {
                API.registerGuiOverlayHandler(GuiCraftingTerminal.class, CraftingTransferHandler.INSTANCE, identifier);
            }
        }
        recipes.clear();
        recipes.add("infusionCrafting");
        recipes.add("cruciblerecipe");
        for (String identifier : recipes) {
            if (!API.hasGuiOverlayHandler(GuiInfusionPatternTerminal.class, identifier)) {
                API.registerGuiOverlayHandler(
                    GuiInfusionPatternTerminal.class,
                    PatternTerminalRecipeTransferHandler.INSTANCE,
                    identifier);
            }
        }
        for (String identifier : FluidRecipe.getSupportRecipes()) {
            if (!API.hasGuiOverlayHandler(GuiWirelessDualInterfaceTerminal.class, identifier)) {
                API.registerGuiOverlayHandler(
                    GuiWirelessDualInterfaceTerminal.class,
                    PatternTerminalRecipeTransferHandler.INSTANCE,
                    identifier);
            }
        }
        API.addOption(new BaseToggleButton(ButtonConstants.HISTORY));
        API.addOption(new BaseToggleButton(ButtonConstants.INVENTORY_STATE));
        API.addOption(new BaseToggleButton(ButtonConstants.ULTRA_TERMINAL_MODE));
        API.addOption(new BaseToggleButton(ButtonConstants.DUAL_INTERFACE_TERMINAL, false));
        API.addOption(new BaseToggleButton(ButtonConstants.PINNED_BAR));
        API.addOption(new BaseToggleButton(ButtonConstants.PINNED_BAR_REMOVE));
        API.addOption(new BaseToggleButton(ButtonConstants.PINNED_BAR_CRAFTING_STATE));
        API.addOption(new BaseToggleButton(ButtonConstants.CRAFTING_NOTIFICATION));
        if (ModAndClassUtil.PH) {
            API.addOption(new BaseToggleButton(ButtonConstants.DUAL_INTERFACE_TERMINAL_FILL_CIRCUIT, false));
        }
        if (ModAndClassUtil.BLOCK_RENDER) {
            API.addOption(new BaseToggleButton(ButtonConstants.BLOCK_RENDER));
        }
    }

    public static boolean getConfigValue(String identifier) {
        return tag.getTag(identifier)
            .getBooleanValue(true);
    }

    @Override
    public String getName() {
        return AE2Thing.NAME;
    }

    @Override
    public String getVersion() {
        return Tags.VERSION;
    }
}
