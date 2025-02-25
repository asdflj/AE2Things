package com.asdflj.ae2thing.api.adapter.pattern;

import net.minecraft.inventory.Container;

public class FCPatternTerminal extends PatternTerminalAdapter {

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
}
