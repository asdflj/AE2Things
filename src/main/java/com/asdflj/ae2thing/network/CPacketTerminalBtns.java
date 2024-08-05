package com.asdflj.ae2thing.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.inventory.Container;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import com.asdflj.ae2thing.client.gui.container.ContainerInfusionPatternTerminal;
import com.asdflj.ae2thing.client.gui.container.ContainerWirelessConnectorTerminal;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class CPacketTerminalBtns implements IMessage {

    private String name = "";
    private String value;
    private NBTTagCompound tag;

    public CPacketTerminalBtns(final String name, final boolean value) {
        this(name, value ? 1 : 0);
    }

    public CPacketTerminalBtns(final String name, final Integer value) {
        this(name, value.toString(), null);
    }

    public CPacketTerminalBtns(final String name, final String value, final NBTTagCompound tag) {
        this.name = name;
        this.value = value;
        this.tag = tag;
    }

    public CPacketTerminalBtns() {
        // NO-OP
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int leName = buf.readInt();
        int leVal = buf.readInt();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < leName; i++) {
            sb.append(buf.readChar());
        }
        name = sb.toString();
        sb = new StringBuilder();
        for (int i = 0; i < leVal; i++) {
            sb.append(buf.readChar());
        }
        value = sb.toString();
        if (buf.readBoolean()) {
            try {
                ByteArrayInputStream bytes = new ByteArrayInputStream(
                    buf.readBytes(buf.readableBytes())
                        .array());
                tag = CompressedStreamTools.readCompressed(bytes);
            } catch (IOException ignored) {

            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(name.length());
        buf.writeInt(value.length());
        for (int i = 0; i < name.length(); i++) {
            buf.writeChar(name.charAt(i));
        }
        for (int i = 0; i < value.length(); i++) {
            buf.writeChar(value.charAt(i));
        }
        buf.writeBoolean(tag != null);
        if (tag != null) {
            try {
                final ByteBuf data = Unpooled.buffer();
                final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                final DataOutputStream outputStream = new DataOutputStream(bytes);

                CompressedStreamTools.writeCompressed(this.tag, outputStream);
                data.writeBytes(bytes.toByteArray());
                data.capacity(data.readableBytes());
                buf.writeBytes(data);

            } catch (IOException ignored) {

            }
        }

    }

    public static class Handler implements IMessageHandler<CPacketTerminalBtns, IMessage> {

        @Override
        public IMessage onMessage(CPacketTerminalBtns message, MessageContext ctx) {
            String name = message.name;
            String value = message.value;
            NBTTagCompound tag = message.tag;
            final Container c = ctx.getServerHandler().playerEntity.openContainer;
            if (name.startsWith("PatternTerminal.") && c instanceof ContainerInfusionPatternTerminal cpt) {
                switch (name) {
                    case "PatternTerminal.Encode" -> {
                        switch (value) {
                            case "0" -> cpt.encode();
                            case "1" -> cpt.encodeAndMoveToInventory();
                            case "3" -> cpt.encodeAllItemAndMoveToInventory();
                        }
                    }
                    case "PatternTerminal.CraftMode" -> cpt.getPatternTerminal()
                        .setCraftingRecipe(value.equals("1"));
                    case "PatternTerminal.Combine" -> cpt.getPatternTerminal()
                        .setCombineMode(value.equals("1"));
                    case "PatternTerminal.Clear" -> cpt.clear();
                    case "PatternTerminal.ActivePage" -> cpt.getPatternTerminal()
                        .setActivePage(Integer.parseInt(value));
                    case "PatternTerminal.Double" -> cpt.doubleStacks(value.equals("1"));
                }
            }
            if (name.startsWith("WirelessConnectorTerminal.") && c instanceof ContainerWirelessConnectorTerminal cwt) {
                switch (name) {
                    case "WirelessConnectorTerminal.SetName" -> cwt.setName(value, tag);
                    case "WirelessConnectorTerminal.Unbind" -> cwt.unBind(tag);
                    case "WirelessConnectorTerminal.Bind" -> cwt.bind(tag);

                }
            }
            return null;
        }
    }
}
