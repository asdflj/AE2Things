package com.asdflj.ae2thing.client.adapter;

import java.util.Objects;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import com.asdflj.ae2thing.api.ICraftingTerminalAdapter;
import com.glodblock.github.client.gui.container.ContainerCraftingWireless;
import com.glodblock.github.inventory.item.IWirelessTerminal;

public class FCCraftingTerminal implements ICraftingTerminalAdapter {

    @Override
    public void openGui(EntityPlayerMP player, TileEntity tile, ForgeDirection face, Object target) {
        com.glodblock.github.inventory.InventoryHandler.openGui(
            player,
            player.worldObj,
            new com.glodblock.github.util.BlockPos(((IWirelessTerminal) target).getInventorySlot(), 0, 0),
            Objects.requireNonNull(face),
            com.glodblock.github.inventory.gui.GuiType.FLUID_CRAFTING_CONFIRM_ITEM);
    }

    @Override
    public Class<? extends Container> getContainer() {
        return ContainerCraftingWireless.class;
    }
}
