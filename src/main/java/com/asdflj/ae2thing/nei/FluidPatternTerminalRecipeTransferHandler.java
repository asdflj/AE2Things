package com.asdflj.ae2thing.nei;

import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.client.gui.GuiDistillationPatternTerminal;
import com.asdflj.ae2thing.nei.object.OrderStack;
import com.asdflj.ae2thing.nei.recipes.FluidRecipe;
import com.asdflj.ae2thing.network.CPacketTransferRecipe;

import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.recipe.IRecipeHandler;

public class FluidPatternTerminalRecipeTransferHandler implements IOverlayHandler {

    public static final FluidPatternTerminalRecipeTransferHandler INSTANCE = new FluidPatternTerminalRecipeTransferHandler();

    @Override
    public void overlayRecipe(GuiContainer firstGui, IRecipeHandler recipe, int recipeIndex, boolean shift) {
        if (firstGui instanceof GuiDistillationPatternTerminal) {
            List<OrderStack<?>> in = FluidRecipe.getPackageInputs(recipe, recipeIndex, false);
            List<OrderStack<?>> out = FluidRecipe.getPackageOutputs(recipe, recipeIndex, false);
            AE2Thing.proxy.netHandler.sendToServer(new CPacketTransferRecipe(out, in, true, shift));
        }
    }

}
