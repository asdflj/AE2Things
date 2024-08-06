package com.asdflj.ae2thing.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import net.bdew.ae2stuff.machines.wireless.BlockWireless;
import net.bdew.ae2stuff.machines.wireless.TileWireless;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.client.gui.GuiWirelessConnectorTerminal;
import com.asdflj.ae2thing.util.Info;

import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import scala.Option;

public class SPacketWirelessConnectorUpdate implements IMessage {

    private final List<TileWireless> tiles = new ArrayList<>();
    private final List<Info> infos = new ArrayList<>();

    public SPacketWirelessConnectorUpdate() {
        // NO-OP
    }

    public SPacketWirelessConnectorUpdate(List<TileWireless> list) {
        tiles.addAll(
            list.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            int size = buf.readShort();
            ByteArrayInputStream bytes = new ByteArrayInputStream(
                buf.readBytes(buf.readableBytes())
                    .array());
            final NBTTagCompound comp = CompressedStreamTools.readCompressed(bytes);
            if (comp != null) {
                for (int x = 0; x < size; x++) {
                    final NBTTagCompound tag = (NBTTagCompound) comp.getTag("#" + x);
                    DimensionalCoord a = DimensionalCoord.readFromNBT(tag);
                    String name = tag.getString(Constants.NAME);
                    AEColor color = AEColor.values()[tag.getInteger(Constants.COLOR)];
                    boolean is_linked = tag.getBoolean(Constants.IS_LINKED);
                    int used = tag.getInteger(Constants.USED_CHANNELS);
                    infos.add(
                        new Info(
                            a,
                            tag.hasKey(Constants.LINK)
                                ? DimensionalCoord.readFromNBT((NBTTagCompound) tag.getTag(Constants.LINK))
                                : null,
                            name,
                            color,
                            is_linked,
                            used));
                }
            }
        } catch (Exception ignored) {

        }

    }

    @Override
    public void toBytes(ByteBuf buf) {
        try {
            buf.writeShort(tiles.size());
            NBTTagCompound tag = new NBTTagCompound();
            int count = 0;
            for (TileWireless tile : tiles) {
                // name color other side
                NBTTagCompound data = new NBTTagCompound();
                tile.getLocation()
                    .writeToNBT(data);
                data.setString(
                    Constants.NAME,
                    tile.hasCustomName() ? tile.getCustomName() : BlockWireless.getLocalizedName());
                data.setInteger(
                    Constants.COLOR,
                    tile.getColor()
                        .ordinal());
                data.setBoolean(Constants.IS_LINKED, tile.isLinked());
                data.setInteger(
                    Constants.USED_CHANNELS,
                    tile.connection() != null ? tile.connection()
                        .getUsedChannels() : 0);
                if (tile.isLinked()) {
                    NBTTagCompound t = new NBTTagCompound();
                    Option<TileWireless> other = tile.getLink();
                    if (!other.isEmpty()) {
                        other.get()
                            .getLocation()
                            .writeToNBT(t);
                        data.setTag(Constants.LINK, t);
                    }
                }
                tag.setTag("#" + count, data);
                count++;
            }
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

    public static class Handler implements IMessageHandler<SPacketWirelessConnectorUpdate, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(SPacketWirelessConnectorUpdate message, MessageContext ctx) {
            final GuiScreen gs = Minecraft.getMinecraft().currentScreen;
            if (gs instanceof GuiWirelessConnectorTerminal) {
                ((GuiWirelessConnectorTerminal) gs).postUpdate(message.infos);
            }
            return null;
        }
    }
}
