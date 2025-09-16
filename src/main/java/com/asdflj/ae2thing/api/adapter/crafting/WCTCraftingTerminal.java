package com.asdflj.ae2thing.api.adapter.crafting;

import static appeng.integration.modules.NEIHelpers.NEICraftingHandler.packIngredients;

import java.io.IOException;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.p455w0rd.wirelesscraftingterminal.common.WCTGuiHandler;
import net.p455w0rd.wirelesscraftingterminal.common.container.ContainerWirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;

public class WCTCraftingTerminal implements ICraftingTerminalAdapter {

    @Override
    public void openGui(EntityPlayerMP player, TileEntity tile, ForgeDirection face, Object target) {
        WCTGuiHandler.launchGui(
            Reference.GUI_CRAFT_CONFIRM,
            player,
            player.worldObj,
            (int) player.posX,
            (int) player.posY,
            (int) player.posZ);
    }

    @Override
    public Class<? extends Container> getContainer() {
        return ContainerWirelessCraftingTerminal.class;
    }

    @Override
    public void moveItems(GuiContainer firstGui, IRecipeHandler recipe, int recipeIndex) {
        try {
            final List<PositionedStack> ingredients = recipe.getIngredientStacks(recipeIndex);
            net.p455w0rd.wirelesscraftingterminal.core.sync.network.NetworkHandler.instance.sendToServer(
                new net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketNEIRecipe(
                    packIngredients(firstGui, ingredients, false)));
        } catch (IOException ignored) {}
    }
}
