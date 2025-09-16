package com.asdflj.ae2thing.api.adapter.terminal.item;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.p455w0rd.wirelesscraftingterminal.items.ItemWirelessCraftingTerminal;

import com.asdflj.ae2thing.inventory.InventoryHandler;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.asdflj.ae2thing.util.BaublesUtil;
import com.asdflj.ae2thing.util.BlockPos;
import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.glodblock.github.util.Util;

import appeng.util.Platform;

public class WCTWirelessCraftingTerminalHandler implements ITerminalHandler {

    @Override
    public void openGui(ItemStack item, ITerminalHandler terminal, TerminalItems items, EntityPlayerMP player) {
        if (item.getItem() instanceof ItemWirelessCraftingTerminal) {
            for (int i = 0; i < player.inventory.mainInventory.length; i++) {
                ItemStack stack = player.inventory.getStackInSlot(i);
                if (Platform.isSameItemPrecise(stack, item)) {
                    openGui(player, Util.GuiHelper.encodeType(i, Util.GuiHelper.InvType.PLAYER_INV), stack);
                    return;
                }
            }
            if (!ModAndClassUtil.BAUBLES) return;
            IInventory handler = BaublesUtil.getBaublesInv(player);
            if (handler == null) return;
            for (int i = 0; i < handler.getSizeInventory(); ++i) {
                ItemStack is = handler.getStackInSlot(i);
                if (BaublesUtil.isSameItemPrecise(is, item, i, items)) {
                    openGui(player, Util.GuiHelper.encodeType(i, Util.GuiHelper.InvType.PLAYER_BAUBLES), is);
                    return;
                }
            }
        }
    }

    private void openGui(EntityPlayerMP player, int x, ItemStack is) {
        InventoryHandler.openGui(
            player,
            player.worldObj,
            new BlockPos(x, 0, 0),
            ForgeDirection.UNKNOWN,
            GuiType.WCT_CRAFTING_TERMINAL_BRIDGE);
    }
}
