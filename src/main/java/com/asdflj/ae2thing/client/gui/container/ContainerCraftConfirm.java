package com.asdflj.ae2thing.client.gui.container;

import java.util.Objects;

import net.minecraft.entity.player.InventoryPlayer;

import com.asdflj.ae2thing.common.parts.PartInfusionPatternTerminal;
import com.asdflj.ae2thing.inventory.InventoryHandler;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.asdflj.ae2thing.util.BlockPos;

import appeng.api.networking.security.IActionHost;
import appeng.api.storage.ITerminalHost;

public class ContainerCraftConfirm extends appeng.container.implementations.ContainerCraftConfirm {

    public ContainerCraftConfirm(final InventoryPlayer ip, final ITerminalHost te) {
        super(ip, te);
    }

    @Override
    public void switchToOriginalGUI() {
        GuiType originalGui = null;

        final IActionHost ah = this.getActionHost();
        if (ah instanceof PartInfusionPatternTerminal) {
            originalGui = GuiType.INFUSION_PATTERN_TERMINAL;
        }
        if (this.getOpenContext() != null) {
            InventoryHandler.openGui(
                this.getInventoryPlayer().player,
                getWorld(),
                new BlockPos(
                    this.getOpenContext()
                        .getTile()),
                Objects.requireNonNull(
                    this.getOpenContext()
                        .getSide()),
                originalGui);
        }
    }
}
