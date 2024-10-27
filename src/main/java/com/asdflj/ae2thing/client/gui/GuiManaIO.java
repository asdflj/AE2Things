package com.asdflj.ae2thing.client.gui;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;

import com.asdflj.ae2thing.client.gui.container.ContainerManaIO;
import com.asdflj.ae2thing.common.parts.PartManaImportBus;
import com.asdflj.ae2thing.common.parts.SharedManaBus;
import com.asdflj.ae2thing.util.NameConst;

import appeng.client.gui.implementations.GuiUpgradeable;
import appeng.core.localization.GuiText;

public class GuiManaIO extends GuiUpgradeable {

    private final SharedManaBus bus;

    public GuiManaIO(InventoryPlayer inventoryPlayer, SharedManaBus te) {
        super(new ContainerManaIO(inventoryPlayer, te));
        this.bus = te;
    }

    @Override
    protected GuiText getName() {
        return this.bus instanceof PartManaImportBus ? GuiText.ImportBus : GuiText.ExportBus;
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.fontRendererObj.drawString(
            this.getGuiDisplayName(
                I18n.format(
                    this.bus instanceof PartManaImportBus ? NameConst.GUI_MANA_IMPORT : NameConst.GUI_MANA_EXPORT)),
            8,
            6,
            4210752);
        this.fontRendererObj.drawString(GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752);

        if (this.redstoneMode != null) {
            this.redstoneMode.set(this.cvb.getRedStoneMode());
        }
    }

}
