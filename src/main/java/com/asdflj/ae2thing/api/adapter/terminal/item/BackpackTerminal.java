package com.asdflj.ae2thing.api.adapter.terminal.item;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.Item;

import com.asdflj.ae2thing.common.item.ItemBackpackTerminal;

public class BackpackTerminal implements IItemTerminal {

    public static BackpackTerminal instance = new BackpackTerminal();

    @Override
    public List<Class<? extends Item>> getClasses() {
        return Arrays.asList(ItemBackpackTerminal.class);
    }

    @Override
    public void openCraftAmount() {

    }

}
