package com.asdflj.ae2thing.client.gui.widget;

import static com.asdflj.ae2thing.api.Constants.INACTIVE_COLOR;
import static com.asdflj.ae2thing.api.Constants.SELECTED_COLOR;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.asdflj.ae2thing.client.gui.GuiWirelessConnectorTerminal;
import com.asdflj.ae2thing.client.me.IDisplayRepo;
import com.asdflj.ae2thing.util.Info;
import com.asdflj.ae2thing.util.NameConst;
import com.asdflj.ae2thing.util.Util;

public class Component implements IClickable {

    public static Info activeInfo = null;

    private final IDisplayRepo repo;
    private final int idx;
    private static final int offsetY = 42;
    private final FontRenderer render;
    private final int x;
    private final int y;
    private final int nameWidth;
    private final GuiWirelessConnectorTerminal gui;
    private final METextField textField;
    private final THGuiButton unbind;
    private final THGuiButton bind;

    public Component(IDisplayRepo repo, int idx, GuiWirelessConnectorTerminal gui, int x, int y) {
        this.textField = new METextField(110, 12, this, gui.getGuiLeft(), gui.getGuiTop());
        this.gui = gui;
        this.repo = repo;
        this.idx = idx;
        this.render = gui.getFontRenderer();
        this.x = x;
        this.y = y + offsetY * idx;
        this.nameWidth = this.render.getStringWidth(I18n.format(NameConst.GUI_WIRELESS_CONNECTOR_TERMINAL_NAME) + ": ");
        this.textField.x = x + nameWidth;
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
        this.gui.getClickables()
            .add(this);
        this.gui.getClickables()
            .add(this.bind);
        this.gui.getClickables()
            .add(this.unbind);
        this.gui.getClickables()
            .add(this.textField);
    }

    private String getName(Info info) {
        String name = I18n.format(NameConst.GUI_WIRELESS_CONNECTOR_TERMINAL_NAME) + ": ";
        return this.textField.isVisible() ? name : name + info.getName();
    }

    public void drawBackground(int color) {
        Info info = getInfo();
        if (info == null) return;
        GuiTextField.drawRect(9, +18 + (idx * offsetY), 169, 18 + offsetY * (idx + 1), color);
    }

    private static class MousePos {

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

    public void drawUnbindBtn() {
        this.unbind.visible = true;
        this.bind.visible = false;
        MousePos mouse = new MousePos();
        this.unbind.drawButton(this.gui.mc, mouse.x - this.gui.getGuiLeft(), mouse.y - this.gui.getGuiTop());
    }

    public void drawBindBtn() {
        this.unbind.visible = false;
        this.bind.visible = true;
        MousePos mouse = new MousePos();
        this.bind.drawButton(this.gui.mc, mouse.x - this.gui.getGuiLeft(), mouse.y - this.gui.getGuiTop());
    }

    public void draw() {
        Info info = getInfo();
        if (info == null) return;
        if (this.textField.isVisible()) {
            this.textField.drawTextBox();
        }
        if (activeInfo != null) {
            if (activeInfo.link && Util.isSameDimensionalCoord(activeInfo.b, info.a)) {
                drawBackground(INACTIVE_COLOR);
                drawUnbindBtn();
            } else if (info.equals(activeInfo) && info.link) {
                drawBackground(SELECTED_COLOR);
                drawUnbindBtn();
            } else if (!activeInfo.link && info.equals(activeInfo)) {
                drawBackground(SELECTED_COLOR);
            } else if (!activeInfo.link && info.dim == activeInfo.dim) {
                drawBindBtn();
            }
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
        GL11.glColor4f(255, 255, 255, 255);
        this.gui.bindTextureBack(this.gui.getBackground());
        if (info.link) {
            this.gui.drawTexturedModalRect(10, 32 + offsetY * idx, 224, 0, 16, 16);
        } else {
            this.gui.drawTexturedModalRect(10, 32 + offsetY * idx, 240, 0, 16, 16);
        }
    }

    public boolean isFocused() {
        return this.textField.isFocused();
    }

    public void textboxKeyTyped(char character, int key) {
        this.textField.textboxKeyTyped(character, key);
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
}
