package com.asdflj.ae2thing.client.gui;

import net.minecraft.entity.player.InventoryPlayer;

import com.asdflj.ae2thing.common.parts.PartInfusionPatternTerminal;
import com.asdflj.ae2thing.inventory.InventoryHandler;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.asdflj.ae2thing.inventory.item.WirelessDualInterfaceTerminalInventory;

import appeng.api.storage.ITerminalHost;

public class GuiCraftConfirm extends appeng.client.gui.implementations.GuiCraftConfirm {

    private GuiType originalGui;

    public GuiCraftConfirm(final InventoryPlayer inventoryPlayer, final ITerminalHost te) {
        super(inventoryPlayer, te);
        if (te instanceof PartInfusionPatternTerminal) {
            this.originalGui = GuiType.INFUSION_PATTERN_TERMINAL;
        } else if (te instanceof WirelessDualInterfaceTerminalInventory) {
            this.originalGui = GuiType.WIRELESS_DUAL_INTERFACE_TERMINAL;
        }
    }

    @Override
    public void switchToOriginalGUI() {
        InventoryHandler.switchGui(originalGui);
    }
}
