package com.asdflj.ae2thing.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.api.CraftingDebugCardObject;
import com.asdflj.ae2thing.api.CraftingDebugHelper;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class SPacketCraftingDebugCardUpdate implements IMessage {

    private NBTTagCompound data;
    private CraftingDebugHelper.LimitedSizeLinkedList<CraftingDebugHelper.CraftingInfo> infos;
    private long networkID;
    private CraftingDebugCardObject.Mode mode;

    public SPacketCraftingDebugCardUpdate(long networkID,
        CraftingDebugHelper.LimitedSizeLinkedList<CraftingDebugHelper.CraftingInfo> infos) {
        this(networkID, infos, CraftingDebugCardObject.Mode.Everything);
    }

    public SPacketCraftingDebugCardUpdate(long networkID,
        CraftingDebugHelper.LimitedSizeLinkedList<CraftingDebugHelper.CraftingInfo> infos,
        CraftingDebugCardObject.Mode mode) {
        this.infos = infos;
        this.data = new NBTTagCompound();
        this.networkID = networkID;
        this.mode = mode;
        CraftingDebugHelper.CraftingInfo.writeToNBTList(infos, this.data, this.networkID);
    }

    public SPacketCraftingDebugCardUpdate() {}

    @Override
    public void fromBytes(ByteBuf buf) {
        this.mode = CraftingDebugCardObject.Mode.values()[buf.readByte()];
        try {
            ByteArrayInputStream bytes = new ByteArrayInputStream(
                buf.readBytes(buf.readableBytes())
                    .array());
            final NBTTagCompound comp = CompressedStreamTools.readCompressed(bytes);
            this.networkID = comp.getLong("networkID");
            this.data = comp;
            this.infos = CraftingDebugHelper.CraftingInfo.readFromNBTList(this.data);
        } catch (Exception e) {}

    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(mode.ordinal());
        try {
            final ByteBuf data = Unpooled.buffer();
            final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            final DataOutputStream outputStream = new DataOutputStream(bytes);
            CompressedStreamTools.writeCompressed(this.data, outputStream);
            data.writeBytes(bytes.toByteArray());
            data.capacity(data.readableBytes());
            buf.writeBytes(data);
        } catch (Exception ignored) {}

    }

    public static class Handler implements IMessageHandler<SPacketCraftingDebugCardUpdate, IMessage> {

        @Override
        public IMessage onMessage(SPacketCraftingDebugCardUpdate message, MessageContext ctx) {
            AE2ThingAPI.instance()
                .pushHistory(message.networkID, message.infos);
            CraftingDebugCardObject.sendMessageToPlayer(message.networkID, message.mode);
            AE2ThingAPI.instance()
                .saveHistory();
            return null;
        }
    }
}
