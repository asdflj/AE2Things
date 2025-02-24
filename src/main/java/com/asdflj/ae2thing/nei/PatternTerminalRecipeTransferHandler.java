package com.asdflj.ae2thing.nei;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.client.gui.GuiInfusionPatternTerminal;
import com.asdflj.ae2thing.client.gui.GuiWirelessDualInterfaceTerminal;
import com.asdflj.ae2thing.nei.object.OrderStack;
import com.asdflj.ae2thing.nei.recipes.FluidRecipe;
import com.asdflj.ae2thing.network.CPacketTransferRecipe;
import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.asdflj.ae2thing.util.NEERecipePacker;
import com.asdflj.ae2thing.util.PHUtil;
import com.github.vfyjxf.nee.network.NEENetworkHandler;
import com.github.vfyjxf.nee.network.packet.PacketNEIPatternRecipe;

import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.recipe.IRecipeHandler;
import codechicken.nei.recipe.TemplateRecipeHandler;
import cpw.mods.fml.common.Loader;

public class PatternTerminalRecipeTransferHandler implements IOverlayHandler {

    public static final PatternTerminalRecipeTransferHandler INSTANCE = new PatternTerminalRecipeTransferHandler();

    public static final HashSet<String> notOtherSet = new HashSet<>();
    public static final HashSet<String> craftSet = new HashSet<>();

    static {
        notOtherSet.add("smelting");
        notOtherSet.add("brewing");
        craftSet.add("crafting");
        craftSet.add("crafting2x2");
    }

    @Override
    public void overlayRecipe(GuiContainer firstGui, IRecipeHandler recipe, int recipeIndex, boolean shift) {
        if (Loader.isModLoaded("neenergistics")) {
            PacketNEIPatternRecipe message = NEERecipePacker.packRecipe(recipe, recipeIndex);
            if (message != null) {
                NEENetworkHandler.getInstance()
                    .sendToServer(message);
            }
        }

        if (firstGui instanceof GuiInfusionPatternTerminal) {
            List<OrderStack<?>> in = FluidRecipe.getPackageInputs(recipe, recipeIndex, false);
            List<OrderStack<?>> out = FluidRecipe.getPackageOutputs(recipe, recipeIndex, false);
            AE2Thing.proxy.netHandler.sendToServer(new CPacketTransferRecipe(out, in, true, shift));
        } else if (firstGui instanceof GuiWirelessDualInterfaceTerminal) {
            boolean priority = ((GuiWirelessDualInterfaceTerminal) firstGui).container.prioritize;
            boolean craft = shouldCraft(recipe);
            List<com.glodblock.github.nei.object.OrderStack<?>> in;
            in = com.glodblock.github.nei.recipes.FluidRecipe.getPackageInputs(recipe, recipeIndex, !craft && priority);
            if (ModAndClassUtil.PH && !craft) {
                in = PHUtil.transfer(in);
            }
            List<com.glodblock.github.nei.object.OrderStack<?>> out = com.glodblock.github.nei.recipes.FluidRecipe
                .getPackageOutputs(recipe, recipeIndex, !notUseOther(recipe));
            AE2Thing.proxy.netHandler
                .sendToServer(new CPacketTransferRecipe(transfer(in), transfer(out), craft, shift));
        }
    }

    private boolean notUseOther(IRecipeHandler recipeHandler) {
        TemplateRecipeHandler tRecipe = (TemplateRecipeHandler) recipeHandler;
        return notOtherSet.contains(tRecipe.getOverlayIdentifier());
    }

    private static List<OrderStack<?>> transfer(List<com.glodblock.github.nei.object.OrderStack<?>> input) {
        List<OrderStack<?>> out = new ArrayList<>();
        for (com.glodblock.github.nei.object.OrderStack<?> stack : input) {
            out.add(new OrderStack<>(stack.getStack(), stack.getIndex()));
        }
        return out;
    }

    private boolean shouldCraft(IRecipeHandler recipeHandler) {
        TemplateRecipeHandler tRecipe = (TemplateRecipeHandler) recipeHandler;
        return craftSet.contains(tRecipe.getOverlayIdentifier());
    }

}
