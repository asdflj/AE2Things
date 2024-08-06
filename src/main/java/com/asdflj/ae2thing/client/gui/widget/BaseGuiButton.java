package com.asdflj.ae2thing.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public abstract class BaseGuiButton extends GuiButton implements IClickable {

    protected final int offsetX;
    protected final int offsetY;
    protected final Component component;
    protected final String packetName;
    protected static final Minecraft mc = Minecraft.getMinecraft();

    public BaseGuiButton(int xPos, int yPos, int width, int height, String text, int offsetX, int offsetY,
        Component component, String packetName) {
        super(0, xPos, yPos, width, height, text);
        this.visible = false;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.component = component;
        this.packetName = packetName;
    }

    public BaseGuiButton(int xPos, int yPos, int width, int height, int offsetX, int offsetY, Component component) {
        this(xPos, yPos, width, height, "", offsetX, offsetY, component, "");
    }

    public BaseGuiButton(int xPos, int yPos, int width, int height, int offsetX, int offsetY, Component component,
        String packetName) {
        this(xPos, yPos, width, height, "", offsetX, offsetY, component, packetName);
    }

    @Override
    public boolean mouseClicked(int xPos, int yPos) {
        return super.mousePressed(Minecraft.getMinecraft(), xPos - offsetX, yPos - offsetY);
    }

}
