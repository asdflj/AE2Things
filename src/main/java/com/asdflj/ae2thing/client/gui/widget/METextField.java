package com.asdflj.ae2thing.client.gui.widget;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.nbt.NBTTagCompound;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.network.CPacketTerminalBtns;
import com.asdflj.ae2thing.util.Info;

import appeng.client.gui.widgets.MEGuiTextField;
import appeng.core.localization.GuiColors;

public class METextField extends MEGuiTextField implements IClickable {

    private final Component component;
    private final int offsetX;
    private final int offsetY;

    public METextField(int width, int height, Component component, int offsetX, int offsetY) {
        super(width, height);
        this.component = component;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public void setVisible(boolean visible) {
        this.field.setVisible(visible);
    }

    public void setFocus(boolean focus, int position) {
        super.setFocused(focus);
        this.field.setCursorPosition(position);
    }

    public void setFocus(boolean focus) {
        super.setFocused(focus);
    }

    public void setX(int x) {
        this.field.xPosition = x;
    }

    public void setY(int y) {
        this.field.yPosition = y;
    }

    @Override
    public boolean mouseClicked(int xPos, int yPos) {
        return this.isMouseIn(xPos, yPos);
    }

    @Override
    public boolean isMouseIn(int xCoord, int yCoord) {
        return super.isMouseIn(xCoord - offsetX, yCoord - offsetY);
    }

    public void drawNameMask() {
        if (this.isFocused()) return;
        Info info = component.getInfo();
        if (info == null) return;
        GuiTextField.drawRect(
            this.x - 2,
            this.y + 1,
            this.x - 2 + this.w,
            this.y + 9 + 1,
            GuiColors.SearchboxFocused.getColor());
    }

    @Override
    public void onClick() {
        Component.setActiveInfo(null);
        Info info = component.getInfo();
        if (info == null) {
            unfocused();
            return;
        }
        if (isFocused()) {
            updateName();
        } else {
            this.setText(info.getName());
            this.setVisible(true);
        }
        setFocus(!isFocused());
    }

    public void updateName() {
        Info info = component.getInfo();
        if (info == null || !this.isFocused()) return;
        if (!info.getName()
            .equals(this.getText())) {
            NBTTagCompound tag = new NBTTagCompound();
            info.a.writeToNBT(tag);
            AE2Thing.proxy.netHandler
                .sendToServer(new CPacketTerminalBtns("WirelessConnectorTerminal.SetName", this.getText(), tag));
        }
        this.setVisible(false);
    }

    @Override
    public void unfocused() {
        updateName();
        this.setFocus(false);
        this.setVisible(false);
    }

    @Override
    public int getIndex() {
        return 1;
    }
}
