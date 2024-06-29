package com.asdflj.ae2thing.network;

import net.minecraft.inventory.Container;

import com.asdflj.ae2thing.client.gui.container.ContainerDistillationPatternTerminal;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class CPacketTerminalBtns implements IMessage {

    private String Name = "";
    private String Value = "";

    public CPacketTerminalBtns(final String name, final String value) {
        Name = name;
        Value = value;
    }

    public CPacketTerminalBtns(final String name, final boolean value) {
        this(name, value ? 1 : 0);
    }

    public CPacketTerminalBtns(final String name, final Integer value) {
        Name = name;
        Value = value.toString();
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
        Name = sb.toString();
        sb = new StringBuilder();
        for (int i = 0; i < leVal; i++) {
            sb.append(buf.readChar());
        }
        Value = sb.toString();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(Name.length());
        buf.writeInt(Value.length());
        for (int i = 0; i < Name.length(); i++) {
            buf.writeChar(Name.charAt(i));
        }
        for (int i = 0; i < Value.length(); i++) {
            buf.writeChar(Value.charAt(i));
        }
    }

    public static class Handler implements IMessageHandler<CPacketTerminalBtns, IMessage> {

        @Override
        public IMessage onMessage(CPacketTerminalBtns message, MessageContext ctx) {
            String Name = message.Name;
            String Value = message.Value;
            final Container c = ctx.getServerHandler().playerEntity.openContainer;
            if (Name.startsWith("PatternTerminal.") && (c instanceof final ContainerDistillationPatternTerminal cpt)) {
                switch (Name) {
                    case "PatternTerminal.Encode" -> {
                        switch (Value) {
                            case "0" -> cpt.encode();
                            case "1" -> cpt.encodeAndMoveToInventory();
                            case "3" -> cpt.encodeAllItemAndMoveToInventory();
                        }
                    }
                    case "PatternTerminal.ScanSourceItem" -> cpt.scanSourceItem();
                    case "PatternTerminal.Clear" -> cpt.clear();
                    case "PatternTerminal.Double" -> cpt.doubleStacks(Value.equals("1"));
                }
            }
            return null;
        }
    }
}
