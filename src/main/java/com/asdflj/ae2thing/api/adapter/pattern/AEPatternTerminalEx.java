package com.asdflj.ae2thing.api.adapter.pattern;

import net.minecraft.inventory.Container;

import appeng.container.implementations.ContainerPatternTermEx;

public class AEPatternTerminalEx extends PatternTerminalAdapter {

    @Override
    public Class<? extends Container> getContainer() {
        return ContainerPatternTermEx.class;
    }
}
