package com.asdflj.ae2thing.api.adapter.crafting;

import static appeng.integration.modules.NEIHelpers.NEICraftingHandler.packIngredients;

import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.client.gui.implementations.GuiCraftingTerm;
import appeng.container.implementations.ContainerCraftingTerm;
import appeng.core.AELog;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketNEIRecipe;
import appeng.util.Platform;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;

public class AECraftingTerminal implements ICraftingTerminalAdapter {

    public AECraftingTerminal() {}

    @Override
    public boolean isTile() {
        return true;
    }

    @Override
    public void openGui(EntityPlayerMP player, TileEntity tile, ForgeDirection face, Object target) {
        Platform.openGUI(player, tile, face, GuiBridge.GUI_CRAFTING_CONFIRM);
    }

    @Override
    public Class<? extends Container> getContainer() {
        return ContainerCraftingTerm.class;
    }

    @Override
    public void moveItems(GuiContainer gui, IRecipeHandler recipe, int recipeIndex) {
        try {
            final List<PositionedStack> ingredients = recipe.getIngredientStacks(recipeIndex);
            if (gui instanceof GuiCraftingTerm) {
                PacketNEIRecipe packet = new PacketNEIRecipe(packIngredients(gui, ingredients, false));
                if (packet.size() >= 32 * 1024) {
                    AELog.warn(
                        "Recipe for " + recipe.getRecipeName()
                            + " has too many variants, reduced version will be used");
                    packet = new PacketNEIRecipe(packIngredients(gui, ingredients, true));
                }
                NetworkHandler.instance.sendToServer(packet);
            }
        } catch (final Exception | Error ignored) {}
    }
}
