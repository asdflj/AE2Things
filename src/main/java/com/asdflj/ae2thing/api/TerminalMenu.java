package com.asdflj.ae2thing.api;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.adapter.item.terminal.BackpackTerminal;
import com.asdflj.ae2thing.api.adapter.item.terminal.DualInterfaceTerminal;
import com.asdflj.ae2thing.api.adapter.item.terminal.FCBaseItemTerminal;
import com.asdflj.ae2thing.api.adapter.item.terminal.FCUltraTerminal;
import com.asdflj.ae2thing.api.adapter.item.terminal.IItemTerminal;
import com.asdflj.ae2thing.api.adapter.item.terminal.TerminalItems;
import com.asdflj.ae2thing.api.adapter.item.terminal.WCTWirelessCraftingTerminal;
import com.asdflj.ae2thing.network.CPacketOpenTerminal;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TerminalMenu {

    public static List<IItemTerminal> terminalHandlers = new ArrayList<>();
    private final List<TerminalItems> terminalItems = new ArrayList<>();
    private final List<ItemStack> items = new ArrayList<>();

    public TerminalMenu() {
        for (IItemTerminal handler : terminalHandlers) {
            terminalItems.addAll(handler.getTerminalItems());
        }
        for (TerminalItems t : terminalItems) {
            items.add(t.getTargetItem());
        }
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public List<TerminalItems> getTerminalItems() {
        return terminalItems;
    }

    public void OpenTerminal(int index) {
        try {
            if (index < 0 || index >= terminalItems.size()) {
                return;
            }
            TerminalItems terminal = terminalItems.get(index);
            AE2Thing.proxy.netHandler.sendToServer(new CPacketOpenTerminal(terminal));
        } catch (Exception ignored) {}
    }

    static {
        terminalHandlers.add(new FCUltraTerminal());
        terminalHandlers.add(new FCBaseItemTerminal());
        terminalHandlers.add(new DualInterfaceTerminal());
        terminalHandlers.add(new WCTWirelessCraftingTerminal());
        terminalHandlers.add(new BackpackTerminal());
    }
}
