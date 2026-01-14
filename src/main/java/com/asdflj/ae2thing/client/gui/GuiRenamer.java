package com.asdflj.ae2thing.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.client.gui.container.ContainerRenamer;
import com.asdflj.ae2thing.inventory.InventoryHandler;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.asdflj.ae2thing.inventory.item.WirelessDualInterfaceTerminalInventory;
import com.asdflj.ae2thing.loader.ItemAndBlockHolder;
import com.asdflj.ae2thing.network.CPacketRenamer;

import appeng.api.storage.ITerminalHost;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.client.gui.widgets.IDropToFillTextField;
import appeng.client.gui.widgets.MEGuiTextField;
import appeng.core.localization.GuiColors;
import appeng.core.localization.GuiText;

public class GuiRenamer extends AEBaseGui implements IDropToFillTextField {

    protected final MEGuiTextField textField;

    protected final ITerminalHost host;
    protected GuiTabButton originalGuiBtn;

    protected ItemStack icon = null;

    protected GuiType originalGui;

    public GuiRenamer(InventoryPlayer ip, ITerminalHost monitorable) {
        super(new ContainerRenamer(ip, monitorable));
        this.host = monitorable;
        this.xSize = 256;

        this.textField = new MEGuiTextField(230, 12);
        this.textField.setMaxStringLength(32);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initGui() {
        super.initGui();
        if (host instanceof WirelessDualInterfaceTerminalInventory) {
            icon = ItemAndBlockHolder.ITEM_WIRELESS_DUAL_INTERFACE_TERMINAL.stack();
            originalGui = GuiType.WIRELESS_DUAL_INTERFACE_TERMINAL;
        }
        if (this.icon != null) {
            this.buttonList.add(
                this.originalGuiBtn = new GuiTabButton(
                    this.guiLeft + 231,
                    this.guiTop - 4,
                    this.icon,
                    this.icon.getDisplayName(),
                    itemRender));
            this.originalGuiBtn.setHideEdge(13);
        }
        this.textField.x = this.guiLeft + 12;
        this.textField.y = this.guiTop + 35;
        this.textField.setFocused(true);
        AE2Thing.proxy.netHandler.sendToServer(new CPacketRenamer(CPacketRenamer.Action.GET_TEXT));
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.fontRendererObj.drawString(GuiText.Renamer.getLocal(), 12, 8, GuiColors.RenamerTitle.getColor());
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.bindTexture("guis/renamer.png");
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, this.xSize, this.ySize);
        this.textField.drawTextBox();
    }

    @Override
    protected void mouseClicked(final int xCoord, final int yCoord, final int btn) {
        this.textField.mouseClicked(xCoord, yCoord, btn);
        super.mouseClicked(xCoord, yCoord, btn);
    }

    @Override
    protected void keyTyped(final char character, final int key) {
        if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER) {
            AE2Thing.proxy.netHandler.sendToServer(new CPacketRenamer(this.textField.getText()));
        } else if (!this.textField.textboxKeyTyped(character, key)) {
            super.keyTyped(character, key);
        }
    }

    public boolean isOverTextField(final int mousex, final int mousey) {
        return textField.isMouseIn(mousex, mousey);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == originalGuiBtn) {
            switchGui();
        } else {
            super.actionPerformed(button);
        }
    }

    public void switchGui() {
        if (originalGui != null) {
            InventoryHandler.switchGui(originalGui);
        }
    }

    public void setTextFieldValue(final String displayName, final int mousex, final int mousey, final ItemStack stack) {
        textField.setText(displayName);
    }

    public void postUpdate(String text) {
        this.textField.setText(text);
        this.textField.setCursorPositionEnd();
    }
}
