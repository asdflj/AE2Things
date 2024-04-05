package com.asdflj.ae2thing.network.wrapper;

import java.util.EnumMap;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;

import com.google.common.base.Throwables;

import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.SimpleChannelHandlerWrapper;
import cpw.mods.fml.common.network.simpleimpl.SimpleIndexedCodec;
import cpw.mods.fml.relauncher.Side;
import io.netty.channel.ChannelFutureListener;

/**
 * Copied from {@link cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper} to be able to override IndexedCodec
 * to add packet logging
 */
public class AE2ThingNetworkWrapper {

    protected final EnumMap<Side, FMLEmbeddedChannel> channels;
    protected final AE2ThingIndexedCodec packetCodec;

    public AE2ThingNetworkWrapper(String channelName) {
        packetCodec = new AE2ThingIndexedCodec();
        channels = NetworkRegistry.INSTANCE.newChannel(channelName, packetCodec);
    }

    /**
     * Register a message and it's associated handler. The message will have the supplied discriminator byte. The
     * message handler will be registered on the supplied side (this is the side where you want the message to be
     * processed and acted upon).
     *
     * @param messageHandler     the message handler type
     * @param requestMessageType the message type
     * @param discriminator      a discriminator byte
     * @param side               the side for the handler
     */
    public <REQ extends IMessage, REPLY extends IMessage> void registerMessage(
        Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Class<REQ> requestMessageType, int discriminator,
        Side side) {
        registerMessage(instantiate(messageHandler), requestMessageType, discriminator, side);
    }

    static <REQ extends IMessage, REPLY extends IMessage> IMessageHandler<? super REQ, ? extends REPLY> instantiate(
        Class<? extends IMessageHandler<? super REQ, ? extends REPLY>> handler) {
        try {
            return handler.getDeclaredConstructor()
                .newInstance();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Register a message and it's associated handler. The message will have the supplied discriminator byte. The
     * message handler will be registered on the supplied side (this is the side where you want the message to be
     * processed and acted upon).
     *
     * @param messageHandler     the message handler instance
     * @param requestMessageType the message type
     * @param discriminator      a discriminator byte
     * @param side               the side for the handler
     */
    public <REQ extends IMessage, REPLY extends IMessage> void registerMessage(
        IMessageHandler<? super REQ, ? extends REPLY> messageHandler, Class<REQ> requestMessageType, int discriminator,
        Side side) {
        packetCodec.addDiscriminator(discriminator, requestMessageType);
        FMLEmbeddedChannel channel = channels.get(side);
        String type = channel.findChannelHandlerNameForType(SimpleIndexedCodec.class);
        if (side == Side.SERVER) {
            addServerHandlerAfter(channel, type, messageHandler, requestMessageType);
        } else {
            addClientHandlerAfter(channel, type, messageHandler, requestMessageType);
        }
    }

    private <REQ extends IMessage, REPLY extends IMessage, NH extends INetHandler> void addServerHandlerAfter(
        FMLEmbeddedChannel channel, String type, IMessageHandler<? super REQ, ? extends REPLY> messageHandler,
        Class<REQ> requestType) {
        SimpleChannelHandlerWrapper<REQ, REPLY> handler = getHandlerWrapper(messageHandler, Side.SERVER, requestType);
        channel.pipeline()
            .addAfter(
                type,
                messageHandler.getClass()
                    .getName(),
                handler);
    }

    private <REQ extends IMessage, REPLY extends IMessage, NH extends INetHandler> void addClientHandlerAfter(
        FMLEmbeddedChannel channel, String type, IMessageHandler<? super REQ, ? extends REPLY> messageHandler,
        Class<REQ> requestType) {
        SimpleChannelHandlerWrapper<REQ, REPLY> handler = getHandlerWrapper(messageHandler, Side.CLIENT, requestType);
        channel.pipeline()
            .addAfter(
                type,
                messageHandler.getClass()
                    .getName(),
                handler);
    }

    private <REPLY extends IMessage, REQ extends IMessage> SimpleChannelHandlerWrapper<REQ, REPLY> getHandlerWrapper(
        IMessageHandler<? super REQ, ? extends REPLY> messageHandler, Side side, Class<REQ> requestType) {
        return new SimpleChannelHandlerWrapper<REQ, REPLY>(messageHandler, side, requestType);
    }

    /**
     * Construct a minecraft packet from the supplied message. Can be used where minecraft packets are required, such as
     * {@link TileEntity#getDescriptionPacket}.
     *
     * @param message The message to translate into packet form
     * @return A minecraft {@link Packet} suitable for use in minecraft APIs
     */
    public Packet getPacketFrom(IMessage message) {
        return channels.get(Side.SERVER)
            .generatePacketFrom(message);
    }

    /**
     * Send this message to everyone. The {@link IMessageHandler} for this message type should be on the CLIENT side.
     *
     * @param message The message to send
     */
    public void sendToAll(IMessage message) {
        channels.get(Side.SERVER)
            .attr(FMLOutboundHandler.FML_MESSAGETARGET)
            .set(FMLOutboundHandler.OutboundTarget.ALL);
        channels.get(Side.SERVER)
            .writeAndFlush(message)
            .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    /**
     * Send this message to the specified player. The {@link IMessageHandler} for this message type should be on the
     * CLIENT side.
     *
     * @param message The message to send
     * @param player  The player to send it to
     */
    public void sendTo(IMessage message, EntityPlayerMP player) {
        channels.get(Side.SERVER)
            .attr(FMLOutboundHandler.FML_MESSAGETARGET)
            .set(FMLOutboundHandler.OutboundTarget.PLAYER);
        channels.get(Side.SERVER)
            .attr(FMLOutboundHandler.FML_MESSAGETARGETARGS)
            .set(player);
        channels.get(Side.SERVER)
            .writeAndFlush(message)
            .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    /**
     * Send this message to everyone within a certain range of a point. The {@link IMessageHandler} for this message
     * type should be on the CLIENT side.
     *
     * @param message The message to send
     * @param point   The {@link TargetPoint} around which to send
     */
    public void sendToAllAround(IMessage message, TargetPoint point) {
        channels.get(Side.SERVER)
            .attr(FMLOutboundHandler.FML_MESSAGETARGET)
            .set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
        channels.get(Side.SERVER)
            .attr(FMLOutboundHandler.FML_MESSAGETARGETARGS)
            .set(point);
        channels.get(Side.SERVER)
            .writeAndFlush(message)
            .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    /**
     * Send this message to everyone within the supplied dimension. The {@link IMessageHandler} for this message type
     * should be on the CLIENT side.
     *
     * @param message     The message to send
     * @param dimensionId The dimension id to target
     */
    public void sendToDimension(IMessage message, int dimensionId) {
        channels.get(Side.SERVER)
            .attr(FMLOutboundHandler.FML_MESSAGETARGET)
            .set(FMLOutboundHandler.OutboundTarget.DIMENSION);
        channels.get(Side.SERVER)
            .attr(FMLOutboundHandler.FML_MESSAGETARGETARGS)
            .set(dimensionId);
        channels.get(Side.SERVER)
            .writeAndFlush(message)
            .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    /**
     * Send this message to the server. The {@link IMessageHandler} for this message type should be on the SERVER side.
     *
     * @param message The message to send
     */
    public void sendToServer(IMessage message) {
        channels.get(Side.CLIENT)
            .attr(FMLOutboundHandler.FML_MESSAGETARGET)
            .set(FMLOutboundHandler.OutboundTarget.TOSERVER);
        channels.get(Side.CLIENT)
            .writeAndFlush(message)
            .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }
}
