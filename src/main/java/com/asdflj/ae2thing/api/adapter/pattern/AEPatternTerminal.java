package com.asdflj.ae2thing.api.adapter.pattern;

import net.minecraft.inventory.Container;

import appeng.container.implementations.ContainerPatternTerm;

public class AEPatternTerminal extends PatternTerminalAdapter {

    @Override
    public Class<? extends Container> getContainer() {
        return ContainerPatternTerm.class;
    }

}
