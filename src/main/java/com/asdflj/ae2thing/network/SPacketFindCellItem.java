package com.asdflj.ae2thing.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import com.asdflj.ae2thing.client.render.BlockPosHighlighter;
import com.asdflj.ae2thing.util.CellPos;
import com.asdflj.ae2thing.util.FindITUtil;
import com.asdflj.ae2thing.util.NameConst;

import appeng.api.util.DimensionalCoord;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class SPacketFindCellItem implements IMessage {

    private List<CellPos> cellPosList;

    public SPacketFindCellItem() {}

    public SPacketFindCellItem(List<CellPos> list) {
        this.cellPosList = list;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            ByteArrayInputStream bytes = new ByteArrayInputStream(
                buf.readBytes(buf.readableBytes())
                    .array());
            NBTTagCompound tag = CompressedStreamTools.readCompressed(bytes);
            this.cellPosList = CellPos.readAsListFromNBT(tag);
        } catch (IOException ignored) {

        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NBTTagCompound tag = new NBTTagCompound();
        CellPos.writeListToNBT(tag, cellPosList);
        try {
            final ByteBuf data = Unpooled.buffer();
            final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            final DataOutputStream outputStream = new DataOutputStream(bytes);

            CompressedStreamTools.writeCompressed(tag, outputStream);
            data.writeBytes(bytes.toByteArray());
            data.capacity(data.readableBytes());
            buf.writeBytes(data);

        } catch (IOException ignored) {

        }
    }

    public static class Handler implements IMessageHandler<SPacketFindCellItem, IMessage> {

        @Override
        public IMessage onMessage(SPacketFindCellItem message, MessageContext ctx) {
            List<DimensionalCoord> list = message.cellPosList.stream()
                .map(CellPos::getCoord)
                .collect(Collectors.toList());
            BlockPosHighlighter.highlightBlocks(
                Minecraft.getMinecraft().thePlayer,
                list,
                NameConst.NEI_FIND_CELL_ITEM_HIGHLIGHT,
                NameConst.NEI_FIND_CELL_ITEM_IN_OTHER_DIM);
            FindITUtil.instance.setSlotHighlighter(message.cellPosList);
            Minecraft.getMinecraft().thePlayer.closeScreen();
            return null;
        }
    }
}
