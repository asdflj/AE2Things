package com.asdflj.ae2thing.network;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.asdflj.ae2thing.inventory.InventoryHandler;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.asdflj.ae2thing.util.BlockPos;
import com.asdflj.ae2thing.util.Util;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class CPacketSwitchGuis implements IMessage {

    private GuiType guiType;

    public CPacketSwitchGuis() {}

    public CPacketSwitchGuis(GuiType guiType) {
        this.guiType = guiType;
    }

    @Override
    public void fromBytes(ByteBuf byteBuf) {
        guiType = GuiType.getByOrdinal(byteBuf.readByte());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(guiType != null ? guiType.ordinal() : 0);
    }

    public static class Handler implements IMessageHandler<CPacketSwitchGuis, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(CPacketSwitchGuis message, MessageContext ctx) {
            if (message.guiType == null) {
                return null;
            }
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            World w = player.worldObj;
            if (message.guiType == GuiType.BACKPACK_TERMINAL) {
                int s = Util.findBackPackTerminal(player);
                if (s != -1) {
                    InventoryHandler.openGui(player, w, new BlockPos(s, 0, 0), ForgeDirection.UNKNOWN, message.guiType);
                }
            }
            return null;
        }

    }

}
