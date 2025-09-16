package com.asdflj.ae2thing.api.adapter.terminal.parts;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.Item;

import com.asdflj.ae2thing.api.adapter.terminal.ITerminal;
import com.glodblock.github.common.item.ItemPartFluidPatternTerminal;
import com.glodblock.github.common.item.ItemPartFluidPatternTerminalEx;

import appeng.helpers.InventoryAction;

public class FCPatternTerminal implements ITerminal {

    @Override
    public List<Class<? extends Item>> getClasses() {
        return Arrays.asList(ItemPartFluidPatternTerminal.class, ItemPartFluidPatternTerminalEx.class);
    }

    @Override
    public void openCraftAmount() {
        com.glodblock.github.network.CPacketInventoryAction packet = new com.glodblock.github.network.CPacketInventoryAction(
            InventoryAction.AUTO_CRAFT,
            0,
            0);
        com.glodblock.github.FluidCraft.proxy.netHandler.sendToServer(packet);
    }

}
