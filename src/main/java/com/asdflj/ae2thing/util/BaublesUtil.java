package com.asdflj.ae2thing.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.api.adapter.terminal.item.TerminalItems;

import appeng.util.Platform;
import baubles.api.BaublesApi;
import baubles.common.lib.PlayerHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BaublesUtil {

    public static IInventory getBaublesInv(EntityPlayer player) {
        return BaublesApi.getBaubles(player);
    }

    @SideOnly(Side.SERVER)
    public static void syncBaubles(EntityPlayer player) {
        Side side = FMLCommonHandler.instance()
            .getEffectiveSide();
        if (side == Side.SERVER) {
            for (int a = 0; a < 4; a++) {
                PlayerHandler.getPlayerBaubles(player)
                    .syncSlotToClients(a);
            }
        }
    }

    public static boolean isSameItemPrecise(ItemStack is1, ItemStack is2, int slotIndex, TerminalItems terminalItems) {
        // baubles can't sync inv to client side,so i use slot to make sure is same item
        if (Platform.isSameItem(is1, is2)) {
            int slot = terminalItems.getData()
                .getInteger(Constants.SLOT);
            return slotIndex == slot;
        }
        return false;
    }
}
