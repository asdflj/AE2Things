package com.asdflj.ae2thing.api.adapter.crafting;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import com.asdflj.ae2thing.util.Util;

import appeng.api.storage.data.IDisplayRepo;
import appeng.client.gui.AEBaseGui;
import codechicken.nei.recipe.IRecipeHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface ICraftingTerminalAdapter {

    default boolean isTile() {
        return false;
    }

    void openGui(EntityPlayerMP player, TileEntity tile, ForgeDirection face, Object target);

    Class<? extends Container> getContainer();

    void moveItems(GuiContainer firstGui, IRecipeHandler recipe, int recipeIndex);

    @SideOnly(Side.CLIENT)
    default IDisplayRepo gerRepo(AEBaseGui gui) {
        return Util.getDisplayRepo(gui);
    }
}
