package com.asdflj.ae2thing.api.adapter.terminal.parts;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.Item;

import com.asdflj.ae2thing.api.adapter.terminal.ITerminal;

import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.helpers.InventoryAction;
import appeng.items.parts.ItemMultiPart;

public class AETerminal implements ITerminal {

    @Override
    public List<Class<? extends Item>> getClasses() {
        return Arrays.asList(ItemMultiPart.class);
    }

    @Override
    public void openCraftAmount() {
        NetworkHandler.instance.sendToServer(new PacketInventoryAction(InventoryAction.AUTO_CRAFT, 0, 0L));
    }
}
