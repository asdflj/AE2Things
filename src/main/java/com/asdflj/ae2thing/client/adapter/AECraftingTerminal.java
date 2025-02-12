package com.asdflj.ae2thing.client.adapter;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import com.asdflj.ae2thing.api.ICraftingTerminalAdapter;

import appeng.container.implementations.ContainerCraftingTerm;
import appeng.core.sync.GuiBridge;
import appeng.util.Platform;

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
}
