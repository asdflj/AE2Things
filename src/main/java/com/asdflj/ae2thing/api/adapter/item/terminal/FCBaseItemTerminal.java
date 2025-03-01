package com.asdflj.ae2thing.api.adapter.item.terminal;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.Item;

import com.glodblock.github.common.item.ItemWirelessFluidTerminal;
import com.glodblock.github.common.item.ItemWirelessInterfaceTerminal;
import com.glodblock.github.common.item.ItemWirelessLevelTerminal;
import com.glodblock.github.common.item.ItemWirelessPatternTerminal;

public class FCBaseItemTerminal implements IItemTerminal {

    @Override
    public List<Class<? extends Item>> getClasses() {
        return Arrays.asList(
            ItemWirelessLevelTerminal.class,
            ItemWirelessFluidTerminal.class,
            ItemWirelessInterfaceTerminal.class,
            ItemWirelessPatternTerminal.class);
    }

}
