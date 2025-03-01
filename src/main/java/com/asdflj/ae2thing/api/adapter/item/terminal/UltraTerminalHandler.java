package com.asdflj.ae2thing.api.adapter.item.terminal;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
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
    public void openGui(ItemStack item, ITerminalHandler terminal, TerminalItems terminalItems, EntityPlayer player) {
        if (item == null) return;
        if (item.getItem() instanceof ItemWirelessUltraTerminal itemWirelessTerminal) {
            for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                ItemStack stack = player.inventory.getStackInSlot(i);
                if (Platform.isSameItemPrecise(stack, item)) {
                    itemWirelessTerminal
                        .setMode(ItemWirelessUltraTerminal.readMode(terminalItems.getTargetItem()), stack);
                    openGui(player, i, stack);
                    return;
                }
            }
            if (!ModAndClassUtil.BAUBLES) return;
            IInventory handler = BaublesUtil.getBaublesInv(player);
            if (handler == null) return;
            for (int i = 0; i < handler.getSizeInventory(); ++i) {
                ItemStack is = handler.getStackInSlot(i);
                if (Platform.isSameItemPrecise(is, item)) {
                    itemWirelessTerminal.setMode(ItemWirelessUltraTerminal.readMode(terminalItems.getTargetItem()), is);
                    openGui(player, i, is);
                    return;
                }
            }
        }
    }

    private void openGui(EntityPlayer player, int index, ItemStack source) {
        InventoryHandler.openGui(
            player,
            player.worldObj,
            new BlockPos(
                index,
                Util.GuiHelper.encodeType(
                    guis.indexOf(
                        GuiType.valueOf(
                            ItemWirelessUltraTerminal.readMode(source)
                                .toString())),
                    Util.GuiHelper.GuiType.ITEM),
                1),
            ForgeDirection.UNKNOWN,
            ItemWirelessUltraTerminal.readMode(source));
    }
}
