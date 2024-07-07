package com.asdflj.ae2thing.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.common.parts.PartInfusionPatternTerminal;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.asdflj.ae2thing.loader.ItemAndBlockHolder;
import com.asdflj.ae2thing.network.CPacketCraftRequest;

import appeng.api.storage.ITerminalHost;
import appeng.container.implementations.ContainerCraftAmount;
import appeng.core.localization.GuiText;

public class GuiCraftAmount extends GuiAmount {

    public GuiCraftAmount(final InventoryPlayer inventoryPlayer, final ITerminalHost te) {
        super(new ContainerCraftAmount(inventoryPlayer, te));
    }

    @Override
    public void initGui() {
        super.initGui();
        this.amountBox.setText("1");
        this.amountBox.setSelectionPos(0);
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.fontRendererObj.drawString(GuiText.SelectAmount.getLocal(), 8, 6, 0x404040);
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        super.drawBG(offsetX, offsetY, mouseX, mouseY);
        this.submit.displayString = isShiftKeyDown() ? GuiText.Start.getLocal() : GuiText.Next.getLocal();
        try {
            this.submit.enabled = getAmount() > 0;
        } catch (final NumberFormatException e) {
            this.submit.enabled = false;
        }
        this.amountBox.drawTextBox();
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        super.actionPerformed(btn);
        try {
            if (btn == this.submit && this.submit.enabled) {
                AE2Thing.proxy.netHandler.sendToServer(new CPacketCraftRequest(getAmount(), isShiftKeyDown()));
            }
        } catch (final NumberFormatException e) {
            this.amountBox.setText("1");
        }
    }

    @Override
    protected void setOriginGUI(Object target) {
        if (target instanceof PartInfusionPatternTerminal) {
            this.myIcon = ItemAndBlockHolder.INFUSION_PATTERN_TERMINAL.stack();
            this.originalGui = GuiType.INFUSION_PATTERN_TERMINAL;
        }
    }

    @Override
    protected String getBackground() {
        return "guis/craftAmt.png";
    }
}
