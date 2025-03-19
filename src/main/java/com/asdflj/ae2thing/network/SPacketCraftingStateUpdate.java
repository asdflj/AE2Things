package com.asdflj.ae2thing.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import com.asdflj.ae2thing.crossmod.waila.CraftingStatePreview;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class SPacketCraftingStateUpdate implements IMessage {

    private NBTTagCompound craftingStates;

    public SPacketCraftingStateUpdate() {}

    public SPacketCraftingStateUpdate(NBTTagCompound cpus) {
        this.craftingStates = cpus;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            ByteArrayInputStream bytes = new ByteArrayInputStream(
                buf.readBytes(buf.readableBytes())
                    .array());
            this.craftingStates = CompressedStreamTools.readCompressed(bytes);
        } catch (IOException ignored) {

        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        try {
            final ByteBuf data = Unpooled.buffer();
            final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            final DataOutputStream outputStream = new DataOutputStream(bytes);
            CompressedStreamTools.writeCompressed(craftingStates, outputStream);
            data.writeBytes(bytes.toByteArray());
            data.capacity(data.readableBytes());
            buf.writeBytes(data);

        } catch (IOException ignored) {

        }
    }

    public static class Handler implements IMessageHandler<SPacketCraftingStateUpdate, IMessage> {

        @Override
        public IMessage onMessage(SPacketCraftingStateUpdate message, MessageContext ctx) {
            CraftingStatePreview.readFromNBT(message.craftingStates);
            return null;
        }
    }
}
