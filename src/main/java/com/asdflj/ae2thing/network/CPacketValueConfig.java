package com.asdflj.ae2thing.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import com.asdflj.ae2thing.client.gui.container.ContainerFluidPacketEncoder;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class CPacketValueConfig implements IMessage {

    private long amount;
    private int valueIndex;

    public CPacketValueConfig() {}

    public CPacketValueConfig(long amount, int valueIndex) {
        this.amount = amount;
        this.valueIndex = valueIndex;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.amount = buf.readLong();
        this.valueIndex = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(amount);
        buf.writeInt(valueIndex);
    }

    public static class Handler implements IMessageHandler<CPacketValueConfig, IMessage> {

        @Override

        public IMessage onMessage(CPacketValueConfig message, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().playerEntity;
            Container container = player.openContainer;
            if (container != null) {
                if (container instanceof ContainerFluidPacketEncoder cfp) {
                    cfp.setLevel(message.amount);
                }
            }
            return null;
        }

    }
}
