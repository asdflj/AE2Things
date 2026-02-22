package com.asdflj.ae2thing.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import com.asdflj.ae2thing.client.gui.IInfoTerminal;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class SPacketNBTDataUpdate implements IMessage {

    private NBTTagCompound tag;

    public SPacketNBTDataUpdate() {
        // NO-OP
    }

    public SPacketNBTDataUpdate(NBTTagCompound tag) {
        this.tag = tag;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            ByteArrayInputStream bytes = new ByteArrayInputStream(
                buf.readBytes(buf.readableBytes())
                    .array());
            this.tag = CompressedStreamTools.readCompressed(bytes);
        } catch (IOException ignored) {

        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        try {
            final ByteBuf data = Unpooled.buffer();
            final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            final DataOutputStream outputStream = new DataOutputStream(bytes);
            CompressedStreamTools.writeCompressed(tag, outputStream);
            data.writeBytes(bytes.toByteArray());
            data.capacity(data.readableBytes());
            buf.writeBytes(data);
        } catch (Exception ignored) {

        }
    }

    public static class Handler implements IMessageHandler<SPacketNBTDataUpdate, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(SPacketNBTDataUpdate message, MessageContext ctx) {
            final GuiScreen gs = Minecraft.getMinecraft().currentScreen;
            if (gs instanceof IInfoTerminal) {
                ((IInfoTerminal) gs).postUpdate(message.tag);
            }
            return null;
        }
    }
}
