package com.asdflj.ae2thing.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.network.CPacketTerminalBtns;
import com.asdflj.ae2thing.util.Info;

public class THGuiButton extends GuiButton implements IClickable {

    private final int offsetX;
    private final int offsetY;
    private final Component component;
    private final String packetName;

    public THGuiButton(int xPos, int yPos, int x, int y, String text, int offsetX, int offsetY, Component component,
        String packetName) {
        super(0, xPos, yPos, x, y, text);
        this.visible = false;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.component = component;
        this.packetName = packetName;
    }

    @Override
    public boolean mouseClicked(int xPos, int yPos) {
        return super.mousePressed(Minecraft.getMinecraft(), xPos - offsetX, yPos - offsetY);
    }

    @Override
    public void onClick() {
        Info info = component.getInfo();
        if (info != null && Component.activeInfo != null) {
            sendToServer(packetName, Component.activeInfo, info);
            Component.activeInfo = null;
        }
    }

    @Override
    public int getIndex() {
        return 1;
    }

    private NBTTagCompound writeToNBT(Info info) {
        NBTTagCompound data = new NBTTagCompound();
        info.a.writeToNBT(data);
        return data;
    }

    private void sendToServer(String packetName, Info a, Info b) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("#0", writeToNBT(a));
        tag.setTag("#1", writeToNBT(b));
        AE2Thing.proxy.netHandler.sendToServer(new CPacketTerminalBtns(packetName, a.getName(), tag));
    }
}
