package com.asdflj.ae2thing.api.adapter.item.terminal;

import static net.p455w0rd.wirelesscraftingterminal.common.WCTGuiHandler.launchGui;
import static net.p455w0rd.wirelesscraftingterminal.reference.Reference.GUI_WCT;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public class WCTWirelessCraftingTerminalHandler implements ITerminalHandler {

    @Override
    public void openGui(ItemStack item, ITerminalHandler terminal, TerminalItems items, EntityPlayerMP player) {
        launchGui(GUI_WCT, player, player.worldObj, (int) player.posX, (int) player.posY, (int) player.posZ);
    }
}
