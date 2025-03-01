package com.asdflj.ae2thing.api.adapter.item.terminal;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.p455w0rd.wirelesscraftingterminal.api.IWirelessCraftingTerminalItem;
import net.p455w0rd.wirelesscraftingterminal.client.gui.GuiWirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.core.sync.network.NetworkHandler;
import net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketOpenGui;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;

import cpw.mods.fml.client.FMLClientHandler;

public class WCTWirelessCraftingTerminalHandler implements ITerminalHandler {

    @Override
    public void openGui(ItemStack item, ITerminalHandler terminal, TerminalItems items, EntityPlayer player) {
        IWirelessCraftingTerminalItem wirelessTerm = (IWirelessCraftingTerminalItem) item.getItem();
        if (wirelessTerm != null && wirelessTerm.isWirelessCraftingEnabled(item)) {
            if (!FMLClientHandler.instance()
                .isGUIOpen(GuiWirelessCraftingTerminal.class)) {
                NetworkHandler.instance.sendToServer(new PacketOpenGui(Reference.GUI_WCT));
            }
        }
    }
}
