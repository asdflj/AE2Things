package com.asdflj.ae2thing.api.adapter.item.terminal;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.Item;
import net.p455w0rd.wirelesscraftingterminal.items.ItemWirelessCraftingTerminal;

public class WCTWirelessCraftingTerminal implements IItemTerminal {

    @Override
    public boolean supportBaubles() {
        return true;
    }

    @Override
    public List<Class<? extends Item>> getClasses() {
        return Arrays.asList(ItemWirelessCraftingTerminal.class);
    }

    @Override
    public List<TerminalItems> getTerminalItems() {
        List<TerminalItems> items = IItemTerminal.super.getTerminalItems();
        if (!items.isEmpty()) {
            return Arrays.asList(items.get(0));
        }
        return items;
    }
}
