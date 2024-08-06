package com.asdflj.ae2thing.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatComponentText;

import com.asdflj.ae2thing.util.Info;
import com.asdflj.ae2thing.util.NameConst;

import appeng.api.util.WorldCoord;
import appeng.client.render.BlockPosHighlighter;

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
            if (mc.thePlayer.worldObj.provider.dimensionId != info.a.getDimension()) {
                mc.thePlayer.addChatComponentMessage(
                    new ChatComponentText(
                        I18n.format(NameConst.GUI_WIRELESS_CONNECTOR_TERMINAL_IN_OTHER_DIM, info.a.getDimension())));
            }
            WorldCoord playerPos = new WorldCoord(
                (int) mc.thePlayer.posX,
                (int) mc.thePlayer.posY,
                (int) mc.thePlayer.posZ);
            BlockPosHighlighter.highlightBlock(
                info.a,
                System.currentTimeMillis() + 500 * WorldCoord.getTaxicabDistance(info.a, playerPos),
                true);
            mc.thePlayer.addChatComponentMessage(
                new ChatComponentText(
                    I18n.format(
                        NameConst.GUI_WIRELESS_CONNECTOR_TERMINAL_HIGHLIGHT,
                        info.a.x,
                        info.a.y,
                        info.a.z,
                        info.a.getDimension())));
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
