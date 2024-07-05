package com.asdflj.ae2thing.client.gui.container;

import net.minecraft.inventory.Slot;

public interface IPatternContainer {

    boolean isPatternTerminal();

    boolean hasRefillerUpgrade();

    void refillBlankPatterns(Slot slot);
}
