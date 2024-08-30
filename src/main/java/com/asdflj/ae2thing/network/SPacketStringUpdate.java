package com.asdflj.ae2thing.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import com.asdflj.ae2thing.client.gui.GuiRenamer;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class SPacketStringUpdate implements IMessage {

    private String text;
    private int slot;

    public SPacketStringUpdate() {}

    public SPacketStringUpdate(String text, int slot) {
        this.text = text;
        this.slot = slot;
    }

    public SPacketStringUpdate(String text) {
        this.text = text;
        this.slot = 0;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.slot = buf.readInt();
        int leName = buf.readInt();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < leName; i++) {
            sb.append(buf.readChar());
        }
        this.text = sb.toString();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.slot);
        buf.writeInt(this.text.length());
        for (int i = 0; i < this.text.length(); i++) {
            buf.writeChar(this.text.charAt(i));
        }
    }

    public static class Handler implements IMessageHandler<SPacketStringUpdate, IMessage> {

        @Override
        public IMessage onMessage(SPacketStringUpdate message, MessageContext ctx) {
            final GuiScreen gs = Minecraft.getMinecraft().currentScreen;
            if (gs instanceof GuiRenamer) {
                ((GuiRenamer) gs).postUpdate(message.text);
            }
            return null;
        }
    }
}
