package com.asdflj.ae2thing.client.gui.widget;

import static com.asdflj.ae2thing.api.Constants.INACTIVE_COLOR;
import static com.asdflj.ae2thing.api.Constants.SELECTED_COLOR;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.asdflj.ae2thing.client.gui.GuiWirelessConnectorTerminal;
import com.asdflj.ae2thing.client.me.IDisplayRepo;
import com.asdflj.ae2thing.common.Config;
import com.asdflj.ae2thing.util.Info;
import com.asdflj.ae2thing.util.NameConst;
import com.asdflj.ae2thing.util.Util;

import appeng.api.util.AEColor;

public class Component implements IClickable {

    public static Info activeInfo = null;

    private final IDisplayRepo repo;
    private final int idx;
    private static final int offsetY = 42;
    private final FontRenderer render;
    private final int x;
    private final int y;
    private final GuiWirelessConnectorTerminal gui;
    private final METextField textField;
    private final THGuiButton unbind;
    private final THGuiButton bind;
    private final HighLightButton highLightBtn;
    private THGuiSelection selection;

    public Component(IDisplayRepo repo, int idx, GuiWirelessConnectorTerminal gui, int x, int y) {
        this.textField = new METextField(110, 12, this, gui.getGuiLeft(), gui.getGuiTop());
        this.gui = gui;
        this.repo = repo;
        this.idx = idx;
        this.render = gui.getFontRenderer();
        this.x = x;
        this.y = y + offsetY * idx;
        this.textField.x = x
            + this.render.getStringWidth(I18n.format(NameConst.GUI_WIRELESS_CONNECTOR_TERMINAL_NAME) + ": ");
        this.textField.y = this.y - 2;
        this.textField.setVisible(false);
        this.bind = new THGuiButton(
            this.x + 96,
            this.y + 18,
            40,
            20,
            I18n.format(NameConst.GUI_WIRELESS_CONNECTOR_TERMINAL_BIND),
            gui.getGuiLeft(),
            gui.getGuiTop(),
            this,
            "WirelessConnectorTerminal.Bind");
        this.unbind = new THGuiButton(
            this.x + 96,
            this.y + 18,
            40,
            20,
            I18n.format(NameConst.GUI_WIRELESS_CONNECTOR_TERMINAL_UNBIND),
            gui.getGuiLeft(),
            gui.getGuiTop(),
            this,
            "WirelessConnectorTerminal.Unbind");

        this.highLightBtn = new HighLightButton(
            this.x + render.getStringWidth(I18n.format(NameConst.GUI_WIRELESS_CONNECTOR_TERMINAL_POS) + ": "),
            this.y + 10 * 2,
            0,
            render.FONT_HEIGHT,
            gui.getGuiLeft(),
            gui.getGuiTop(),
            this);
        this.gui.getClickables()
            .add(this);
        this.gui.getClickables()
            .add(this.bind);
        this.gui.getClickables()
            .add(this.unbind);
        this.gui.getClickables()
            .add(this.textField);
        this.gui.getClickables()
            .add(this.highLightBtn);
        if (Config.wirelessConnectorTerminalColorSelection) {
            this.selection = new THGuiSelection(
                x + 85,
                this.y + 11,
                16,
                7,
                gui.getGuiLeft(),
                gui.getGuiTop(),
                this,
                "WirelessConnectorTerminal.Color");
            this.gui.getClickables()
                .add(this.selection);
            this.gui.getScrollables()
                .add(this.selection);
        }
    }

    private String getName(Info info) {
        String name = I18n.format(NameConst.GUI_WIRELESS_CONNECTOR_TERMINAL_NAME) + ": ";
        return this.textField.isVisible() ? name : name + info.getName();
    }

    public void drawBackground(int color) {
        Info info = getInfo();
        if (info == null) return;
        GuiTextField.drawRect(9, 18 + (idx * offsetY), 169, 18 + offsetY * (idx + 1), color);
    }

    public static class MousePos {

        public final int x;
        public final int y;

        public MousePos() {
            Minecraft mc = Minecraft.getMinecraft();
            final ScaledResolution scaledresolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
            int i = scaledresolution.getScaledWidth();
            int j = scaledresolution.getScaledHeight();
            x = Mouse.getX() * i / mc.displayWidth;
            y = j - Mouse.getY() * j / mc.displayHeight - 1;
        }
    }

    public Info getInfo() {
        return repo.getInfo(idx);
    }

    public void drawUnbindBtn(MousePos mouse) {
        this.unbind.visible = true;
        this.bind.visible = false;
        this.unbind.drawButton(this.gui.mc, mouse.x - this.gui.getGuiLeft(), mouse.y - this.gui.getGuiTop());
    }

    public void drawBindBtn(MousePos mouse) {
        this.unbind.visible = false;
        this.bind.visible = true;
        this.bind.drawButton(this.gui.mc, mouse.x - this.gui.getGuiLeft(), mouse.y - this.gui.getGuiTop());
    }

    private void drawSelection(MousePos mouse) {
        this.drawSelection(mouse.x, mouse.y);
    }

    public void drawSelection(int mouseX, int mouseY) {
        if (this.selection != null) {
            this.selection.drawButton(this.gui.mc, mouseX - this.gui.getGuiLeft(), mouseY - this.gui.getGuiTop());
        }
    }

    public void draw() {
        Info info = getInfo();
        if (info == null) return;
        MousePos mouse = new MousePos();
        this.highLightBtn.setWidth(render.getStringWidth(info.getPosString()));
        if (this.textField.isVisible()) {
            this.textField.drawTextBox();
        }
        if (activeInfo != null) {
            if (activeInfo.link && Util.isSameDimensionalCoord(activeInfo.b, info.a)) {
                drawBackground(INACTIVE_COLOR);
                drawUnbindBtn(mouse);
            } else if (info.equals(activeInfo) && info.link) {
                drawBackground(SELECTED_COLOR);
                drawUnbindBtn(mouse);
            } else if (!activeInfo.link && info.equals(activeInfo)) {
                drawBackground(SELECTED_COLOR);
            } else if (!activeInfo.link && info.dim == activeInfo.dim) {
                drawBindBtn(mouse);
            }
        } else {
            this.unbind.visible = false;
            this.bind.visible = false;
        }
        this.render.drawString(this.getName(info), x, y, 4210752);
        this.render.drawString(
            I18n.format(NameConst.GUI_WIRELESS_CONNECTOR_TERMINAL_COLOR) + ": " + info.getColor(),
            x,
            y + 10,
            4210752);
        this.render.drawString(
            I18n.format(NameConst.GUI_WIRELESS_CONNECTOR_TERMINAL_POS) + ": " + info.getPosString(),
            x,
            y + 10 * 2,
            4210752);
        this.render.drawString(
            I18n.format(NameConst.GUI_WIRELESS_CONNECTOR_TERMINAL_CHANNELS_USED) + ": " + info.getChannelsUsed(),
            x,
            y + 10 * 3,
            4210752);
        drawWirelessConnector(info);
        this.drawSelection(mouse);
    }

    private void drawWirelessConnector(Info info) {
        GL11.glColor4f(255, 255, 255, 255);
        this.gui.bindTextureBack(this.gui.getBackground());
        if (this.selection != null) {
            this.gui.drawTexturedModalRect(this.selection.xPosition, this.selection.yPosition, 224, 32, 16, 16);
        }
        if (info.link) {
            this.gui.drawTexturedModalRect(10, 40 + offsetY * idx, 224, 0, 16, 16);
            if (info.getAEColor() == AEColor.Transparent) {
                this.gui.drawTexturedModalRect(10, 20 + offsetY * idx, 224, 16, 16, 16);
            } else {
                this.gui.drawTexturedModalRect(
                    10,
                    20 + offsetY * idx,
                    208,
                    info.getAEColor()
                        .ordinal() * 16,
                    16,
                    16);
            }
        } else {
            this.gui.drawTexturedModalRect(10, 40 + offsetY * idx, 240, 0, 16, 16);
            this.gui.drawTexturedModalRect(10, 20 + offsetY * idx, 240, 16, 16, 16);
        }
    }

    public boolean isFocused() {
        return this.textField.isFocused();
    }

    public void textboxKeyTyped(char character, int key) {
        if (key == Keyboard.KEY_RETURN) {
            this.textField.unfocused();
        } else {
            this.textField.textboxKeyTyped(character, key);
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY) {
        final boolean withinXRange = this.gui.getGuiLeft() + 9 <= mouseX && mouseX < this.gui.getGuiLeft() + 160 + 9;
        final boolean withinYRange = y <= mouseY && mouseY < this.y + offsetY;
        return withinXRange && withinYRange;
    }

    @Override
    public void onClick() {
        Info info = this.repo.getInfo(idx);
        if (info == null) return;
        setActiveInfo(info);
    }

    @Override
    public int getIndex() {
        return 0;
    }

    public static void setActiveInfo(Info info) {
        activeInfo = info;
    }

    public GuiWirelessConnectorTerminal getGui() {
        return gui;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public FontRenderer getRender() {
        return render;
    }
}
