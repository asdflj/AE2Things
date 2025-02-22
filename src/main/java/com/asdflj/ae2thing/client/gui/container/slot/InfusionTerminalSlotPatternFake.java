package com.asdflj.ae2thing.client.gui.container.slot;

import net.minecraft.inventory.IInventory;

import appeng.container.slot.IOptionalSlotHost;

public class InfusionTerminalSlotPatternFake extends SlotPatternFake {

    public InfusionTerminalSlotPatternFake(IInventory inv, IOptionalSlotHost containerBus, int idx, int x, int y,
        int offX, int offY, int groupNum) {
        super(inv, containerBus, idx, x, y, offX, offY, groupNum);
    }
}
