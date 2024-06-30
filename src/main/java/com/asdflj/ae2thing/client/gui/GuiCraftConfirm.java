package com.asdflj.ae2thing.client.gui;

import net.minecraft.entity.player.InventoryPlayer;

import com.asdflj.ae2thing.common.parts.PartDistillationPatternTerminal;
import com.asdflj.ae2thing.inventory.InventoryHandler;
import com.asdflj.ae2thing.inventory.gui.GuiType;

import appeng.api.storage.ITerminalHost;

public class GuiCraftConfirm extends appeng.client.gui.implementations.GuiCraftConfirm {

    private GuiType originalGui;

    public GuiCraftConfirm(final InventoryPlayer inventoryPlayer, final ITerminalHost te) {
        super(inventoryPlayer, te);
        if (te instanceof PartDistillationPatternTerminal) {
            this.originalGui = GuiType.DISTILLATION_PATTERN_TERMINAL;
        }
    }

    @Override
    public void switchToOriginalGUI() {
        InventoryHandler.switchGui(originalGui);
    }
}
