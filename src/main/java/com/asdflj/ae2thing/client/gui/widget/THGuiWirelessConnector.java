package com.asdflj.ae2thing.client.gui.widget;

import com.asdflj.ae2thing.client.me.IDisplayRepo;
import com.asdflj.ae2thing.util.Info;
import com.asdflj.ae2thing.util.NameConst;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;

public class THGuiWirelessConnector {
    private final IDisplayRepo repo;
    private final int idx;
    private static final int offsetY = 42;
    private final FontRenderer render;
    private final int x;
    private final int y;
    private final GuiTextField text;

    public THGuiWirelessConnector(IDisplayRepo repo, int idx, FontRenderer render,int x,int y) {
        this.repo = repo;
        this.idx = idx;
        this.render = render;
        this.x = x;
        this.y = y;
        this.text = new GuiTextField(this.render,x,y,100,this.render.FONT_HEIGHT);
        this.text.setVisible(false);
    }


    public void draw() {
        Info info = this.repo.getInfo(idx);
        if(info == null) return;
        this.render.drawStringWithShadow(I18n.format(NameConst.GUI_WIRELESS_CONNECTOR_TERMINAL_NAME)+ ":" + info.getName(),x, y + offsetY * idx, 0xffffff);
        this.render.drawStringWithShadow(I18n.format(NameConst.GUI_WIRELESS_CONNECTOR_TERMINAL_COLOR)+":" + info.getColor(),x, y+ 10 + offsetY * idx, 0xffffff);
        this.render.drawStringWithShadow(I18n.format(NameConst.GUI_WIRELESS_CONNECTOR_TERMINAL_POS) +":"+ info.getPosString(),x, y+10 *2 + offsetY * idx, 0xffffff);
        this.render.drawStringWithShadow(I18n.format(NameConst.GUI_WIRELESS_CONNECTOR_TERMINAL_CHANNELS_USED)+":" + info.getChannelsUsed(),x, y+10 * 3 + offsetY * idx, 0xffffff);

    }

    public boolean isMouseIn(int mouseX, int mouseY) {
        Info info = this.repo.getInfo(idx);
        if(info == null) return false;
        final boolean withinXRange = this.x <= mouseX && mouseX < this.x + 100;
        final boolean withinYRange = this.y <= mouseY && mouseY < this.y + this.render.FONT_HEIGHT;
        return withinXRange && withinYRange;
    }

    public void drawNameMask() {
        this.text.setVisible(true);
        this.text.setFocused(true);
        this.text.drawTextBox();
    }
}
