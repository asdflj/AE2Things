package com.asdflj.ae2thing.network.wrapper;

import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.features.AEFeature;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.SimpleIndexedCodec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class AE2ThingIndexedCodec extends SimpleIndexedCodec {

    static int DISCRIMINATOR_BYTE = 1;

    @Override
    public void encodeInto(ChannelHandlerContext ctx, IMessage msg, ByteBuf target) throws Exception {
        super.encodeInto(ctx, msg, target);

        if (AEConfig.instance.isFeatureEnabled(AEFeature.PacketLogging)) {
            AELog.info(
                " -> " + msg.getClass()
                    .getName() + " : " + target.readableBytes());
        }
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf source, IMessage msg) {
        if (AEConfig.instance.isFeatureEnabled(AEFeature.PacketLogging)) {
            AELog.info(
                " <- " + msg.getClass()
                    .getName() + " : " + (source.readableBytes() + DISCRIMINATOR_BYTE));
        }

        super.decodeInto(ctx, source, msg);
    }
}
