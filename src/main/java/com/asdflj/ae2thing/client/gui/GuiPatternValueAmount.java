package com.asdflj.ae2thing.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.client.gui.container.ContainerPatternValueAmount;
import com.asdflj.ae2thing.common.parts.PartInfusionPatternTerminal;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.asdflj.ae2thing.inventory.item.PatternModifierInventory;
import com.asdflj.ae2thing.inventory.item.WirelessDualInterfaceTerminalInventory;
import com.asdflj.ae2thing.loader.ItemAndBlockHolder;
import com.asdflj.ae2thing.network.CPacketPatternValueSet;

import appeng.api.storage.ITerminalHost;
import appeng.core.localization.GuiText;

public class GuiPatternValueAmount extends GuiAmount {

    public GuiPatternValueAmount(InventoryPlayer inventoryPlayer, ITerminalHost te) {
        super(new ContainerPatternValueAmount(inventoryPlayer, te));
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        super.actionPerformed(btn);
        try {
            if (btn == this.submit && btn.enabled) {
                AE2Thing.proxy.netHandler.sendToServer(
                    new CPacketPatternValueSet(
                        originalGui.ordinal(),
                        getAmount(),
                        ((ContainerPatternValueAmount) this.inventorySlots).getValueIndex()));
            }
        } catch (final NumberFormatException e) {
            this.amountBox.setText("1");
        }
    }

    protected void setOriginGUI(Object target) {
        if (target instanceof PartInfusionPatternTerminal) {
            this.myIcon = ItemAndBlockHolder.INFUSION_PATTERN_TERMINAL.stack();
            this.originalGui = GuiType.INFUSION_PATTERN_TERMINAL;
        } else if (target instanceof WirelessDualInterfaceTerminalInventory) {
            this.myIcon = ItemAndBlockHolder.ITEM_WIRELESS_DUAL_INTERFACE_TERMINAL.stack();
            this.originalGui = GuiType.WIRELESS_DUAL_INTERFACE_TERMINAL;
        } else if (target instanceof PatternModifierInventory) {
            this.myIcon = ItemAndBlockHolder.ITEM_PATTERN_MODIFIER.stack();
            this.originalGui = GuiType.PATTERN_MODIFIER;
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        this.submit.displayString = GuiText.Set.getLocal();
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.fontRendererObj.drawString(GuiText.SelectAmount.getLocal(), 8, 6, 0x404040);
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        super.drawBG(offsetX, offsetY, mouseX, mouseY);
        try {
            int result = getAmount();
            this.submit.enabled = result > 0;
        } catch (final NumberFormatException e) {
            this.submit.enabled = false;
        }
        this.amountBox.drawTextBox();
    }

    protected String getBackground() {
        return "guis/craftAmt.png";
    }

    @Override
    public void setAmount(int amount) {
        this.amountBox.setText(String.valueOf(amount));
        this.amountBox.setCursorPositionEnd();
        this.amountBox.setSelectionPos(0);
    }
}
