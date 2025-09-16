package com.asdflj.ae2thing.api.adapter.terminal.item;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import com.asdflj.ae2thing.common.item.ItemBackpackTerminal;
import com.asdflj.ae2thing.inventory.InventoryHandler;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.asdflj.ae2thing.util.BlockPos;

import appeng.util.Platform;

public class BackpackTerminalHandler implements ITerminalHandler {

    @Override
    public boolean canConnect(ItemStack item, ITerminalHandler terminal, TerminalItems items, EntityPlayerMP player) {
        return true;
    }

    @Override
    public void openGui(ItemStack item, ITerminalHandler terminal, TerminalItems items, EntityPlayerMP player) {
        if (item.getItem() instanceof ItemBackpackTerminal) {
            for (int i = 0; i < player.inventory.mainInventory.length; i++) {
                ItemStack stack = player.inventory.getStackInSlot(i);
                if (Platform.isSameItemPrecise(stack, item)) {
                    InventoryHandler.openGui(
                        player,
                        player.worldObj,
                        new BlockPos(i, 0, 0),
                        ForgeDirection.UNKNOWN,
                        GuiType.BACKPACK_TERMINAL);
                    return;
                }
            }
        }
    }
}
