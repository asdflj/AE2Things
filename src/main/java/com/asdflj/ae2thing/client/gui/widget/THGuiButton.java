package com.asdflj.ae2thing.client.gui.widget;

import net.minecraft.nbt.NBTTagCompound;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.network.CPacketTerminalBtns;
import com.asdflj.ae2thing.util.Info;

public class THGuiButton extends BaseGuiButton {

    public THGuiButton(int xPos, int yPos, int width, int height, String text, int offsetX, int offsetY,
        Component component, String packetName) {
        super(xPos, yPos, width, height, text, offsetX, offsetY, component, packetName);
    }

    @Override
    public void onClick() {
        Info info = component.getInfo();
        if (info != null && Component.activeInfo != null) {
            sendToServer(packetName, Component.activeInfo, info);
            Component.setActiveInfo(null);
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
