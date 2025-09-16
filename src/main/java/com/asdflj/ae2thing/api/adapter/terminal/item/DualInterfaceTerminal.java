package com.asdflj.ae2thing.api.adapter.terminal.item;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.Item;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.common.item.ItemWirelessDualInterfaceTerminal;
import com.asdflj.ae2thing.network.CPacketInventoryAction;

import appeng.helpers.InventoryAction;

public class DualInterfaceTerminal implements IItemTerminal {

    public static DualInterfaceTerminal instance = new DualInterfaceTerminal();

    @Override
    public List<Class<? extends Item>> getClasses() {
        return Arrays.asList(ItemWirelessDualInterfaceTerminal.class);
    }

    @Override
    public void openCraftAmount() {
        CPacketInventoryAction packet = new CPacketInventoryAction(InventoryAction.AUTO_CRAFT, 0, 0);
        AE2Thing.proxy.netHandler.sendToServer(packet);
    }

}
