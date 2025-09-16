package com.asdflj.ae2thing.api.adapter.terminal.item;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.Item;
import net.p455w0rd.wirelesscraftingterminal.items.ItemWirelessCraftingTerminal;

import appeng.helpers.InventoryAction;

public class WCTWirelessCraftingTerminal implements IItemTerminal {

    public static WCTWirelessCraftingTerminal instance = new WCTWirelessCraftingTerminal();

    @Override
    public boolean supportBaubles() {
        return true;
    }

    @Override
    public List<Class<? extends Item>> getClasses() {
        return Arrays.asList(ItemWirelessCraftingTerminal.class);
    }

    @Override
    public void openCraftAmount() {
        net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketInventoryAction packet = new net.p455w0rd.wirelesscraftingterminal.core.sync.packets.PacketInventoryAction(
            InventoryAction.AUTO_CRAFT,
            0,
            0);
        net.p455w0rd.wirelesscraftingterminal.core.sync.network.NetworkHandler.instance.sendToServer(packet);
    }

}
