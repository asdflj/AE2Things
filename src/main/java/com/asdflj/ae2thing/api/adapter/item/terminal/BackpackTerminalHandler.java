package com.asdflj.ae2thing.api.adapter.item.terminal;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import com.asdflj.ae2thing.common.item.ItemBackpackTerminal;
import com.asdflj.ae2thing.inventory.InventoryHandler;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.asdflj.ae2thing.util.BlockPos;

import appeng.util.Platform;

public class BackpackTerminalHandler implements ITerminalHandler {

    @Override
    public boolean canConnect(ItemStack item, ITerminalHandler terminal, TerminalItems items, EntityPlayer player) {
        return true;
    }

    @Override
    public void openGui(ItemStack item, ITerminalHandler terminal, TerminalItems items, EntityPlayer player) {
        if (item.getItem() instanceof ItemBackpackTerminal) {
            for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                ItemStack stack = player.inventory.getStackInSlot(i);
                if (Platform.isSameItemPrecise(stack, item)) {
                    InventoryHandler.openGui(
                        player,
                        player.getEntityWorld(),
                        new BlockPos(i, 0, 0),
                        ForgeDirection.UNKNOWN,
                        GuiType.BACKPACK_TERMINAL);
                    return;
                }
            }
        }
    }
}
