package com.asdflj.ae2thing.client.gui.widget;

import java.util.Collections;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import com.asdflj.ae2thing.client.render.BlockPosHighlighter;
import com.asdflj.ae2thing.util.Info;
import com.asdflj.ae2thing.util.NameConst;

import appeng.core.localization.PlayerMessages;

public class HighLightButton extends BaseGuiButton {

    private final Minecraft mc = Minecraft.getMinecraft();

    public HighLightButton(int xPos, int yPos, int width, int height, int offsetX, int offsetY, Component component) {
        super(xPos, yPos, width, height, offsetX, offsetY, component);
        this.visible = true;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {

    }

    @Override
    public void onClick() {
        Info info = component.getInfo();
        if (info != null) {
            BlockPosHighlighter.highlightBlocks(
                mc.thePlayer,
                Collections.singletonList(info.a),
                PlayerMessages.InterfaceHighlighted.getName(),
                I18n.format(NameConst.GUI_WIRELESS_CONNECTOR_TERMINAL_IN_OTHER_DIM, info.a.getDimension()));
            mc.thePlayer.closeScreen();
        }
    }

    @Override
    public int getIndex() {
        return 1;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}
