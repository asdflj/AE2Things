package com.asdflj.ae2thing.api.adapter.terminal;

import java.util.List;

import net.minecraft.item.Item;

public interface ITerminal {

    List<Class<? extends Item>> getClasses();

    void openCraftAmount();

}
