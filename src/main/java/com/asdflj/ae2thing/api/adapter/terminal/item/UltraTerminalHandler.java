package com.asdflj.ae2thing.api.adapter.terminal.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import com.asdflj.ae2thing.util.BaublesUtil;
import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.glodblock.github.common.item.ItemWirelessUltraTerminal;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.Util;

import appeng.util.Platform;

public class UltraTerminalHandler implements ITerminalHandler {

    private static final List<GuiType> guis = ItemWirelessUltraTerminal.getGuis();

    @Override
    public void openGui(ItemStack item, ITerminalHandler terminal, TerminalItems terminalItems, EntityPlayerMP player) {
        if (item == null) return;
        if (item.getItem() instanceof ItemWirelessUltraTerminal) {
            for (int i = 0; i < player.inventory.mainInventory.length; i++) {
                ItemStack stack = player.inventory.getStackInSlot(i);
                if (Platform.isSameItemPrecise(stack, item)) {
                    player.inventory.setInventorySlotContents(i, terminalItems.getTargetItem());
                    openGui(
                        player,
                        Util.GuiHelper.encodeType(i, Util.GuiHelper.InvType.PLAYER_INV),
                        terminalItems.getTargetItem());
                    return;
                }
            }
            if (!ModAndClassUtil.BAUBLES) return;
            IInventory handler = BaublesUtil.getBaublesInv(player);
            if (handler == null) return;
            for (int i = 0; i < handler.getSizeInventory(); ++i) {
                ItemStack is = handler.getStackInSlot(i);
                if (BaublesUtil.isSameItemPrecise(is, item, i, terminalItems)) {
                    handler.setInventorySlotContents(i, terminalItems.getTargetItem());
                    openGui(
                        player,
                        Util.GuiHelper.encodeType(i, Util.GuiHelper.InvType.PLAYER_BAUBLES),
                        terminalItems.getTargetItem());
                    return;
                }
            }
        }
    }

    private void openGui(EntityPlayerMP player, int x, ItemStack is) {
        GuiType type = ItemWirelessUltraTerminal.readMode(is);
        InventoryHandler.openGui(
            player,
            player.worldObj,
            new BlockPos(x, Util.GuiHelper.encodeType(guis.indexOf(type), Util.GuiHelper.GuiType.ITEM), 1),
            ForgeDirection.UNKNOWN,
            type);
    }
}
