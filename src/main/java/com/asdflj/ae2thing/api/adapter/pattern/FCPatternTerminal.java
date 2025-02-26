package com.asdflj.ae2thing.api.adapter.pattern;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;

import com.glodblock.github.inventory.item.IItemPatternTerminal;

import appeng.container.AEBaseContainer;

public class FCPatternTerminal implements IPatternTerminalAdapter {

    private final Class<? extends Container> container;

    public FCPatternTerminal(Class<? extends Container> containerClass) {
        this.container = containerClass;
    }

    @Override
    public boolean supportFluid() {
        return true;
    }

    @Override
    public Class<? extends Container> getContainer() {
        return this.container;
    }

    @Override
    public IInventory getInventoryByName(Container container, String name) {
        if (container instanceof AEBaseContainer c) {
            if (c.getTarget() instanceof IItemPatternTerminal t) {
                return t.getInventoryByName(name);
            }
        }
        return null;
    }

}
