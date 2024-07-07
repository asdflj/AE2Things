package com.asdflj.ae2thing.network;

import net.minecraft.inventory.Container;

import com.asdflj.ae2thing.client.gui.container.ContainerDistillationPatternTerminal;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class CPacketTerminalBtns implements IMessage {

    private String name = "";
    private int value;

    public CPacketTerminalBtns(final String name, final boolean value) {
        this(name, value ? 1 : 0);
    }

    public CPacketTerminalBtns(final String name, final Integer value) {
        this.name = name;
        this.value = value;
    }

    public CPacketTerminalBtns() {
        // NO-OP
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int leName = buf.readInt();
        StringBuilder sb = new StringBuilder();;
        for (int i = 0; i < leName; i++) {
            sb.append(buf.readChar());
        }
        name = sb.toString();
        value = buf.readByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(name.length());
        for (int i = 0; i < name.length(); i++) {
            buf.writeChar(name.charAt(i));
        }
        buf.writeByte(value);
    }

    public static class Handler implements IMessageHandler<CPacketTerminalBtns, IMessage> {

        @Override
        public IMessage onMessage(CPacketTerminalBtns message, MessageContext ctx) {
            String name = message.name;
            int value = message.value;
            final Container c = ctx.getServerHandler().playerEntity.openContainer;
            if (name.startsWith("PatternTerminal.") && c instanceof ContainerDistillationPatternTerminal cpt) {
                switch (name) {
                    case "PatternTerminal.Encode" -> {
                        switch (value) {
                            case 0 -> cpt.encode();
                            case 1 -> cpt.encodeAndMoveToInventory();
                            case 3 -> cpt.encodeAllItemAndMoveToInventory();
                        }
                    }
                    case "PatternTerminal.CraftMode" -> cpt.getPatternTerminal()
                        .setCraftingRecipe(value == 1);
                    case "PatternTerminal.Clear" -> cpt.clear();
                    case "PatternTerminal.Double" -> cpt.doubleStacks(value == 1);
                }
            }
            return null;
        }
    }
}
