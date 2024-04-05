package com.asdflj.ae2thing.inventory.gui;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface IGuiFactory {

    @Nullable
    Object createServerGui(EntityPlayer player, World world, int x, int y, int z, ForgeDirection face);

    @SideOnly(Side.CLIENT)
    @Nullable
    Object createClientGui(EntityPlayer player, World world, int x, int y, int z, ForgeDirection face);
}
