package com.asdflj.ae2thing.api;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public interface ICraftingTerminalAdapter {

    default boolean isTile() {
        return false;
    }

    void openGui(EntityPlayerMP player, TileEntity tile, ForgeDirection face, Object target);

    Class<? extends Container> getContainer();
}
