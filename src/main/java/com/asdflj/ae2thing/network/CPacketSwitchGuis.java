package com.asdflj.ae2thing.network;

import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.asdflj.ae2thing.inventory.InventoryHandler;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.asdflj.ae2thing.inventory.item.WirelessTerminal;
import com.asdflj.ae2thing.util.BlockPos;
import com.asdflj.ae2thing.util.Util;

import appeng.client.gui.AEBaseGui;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class CPacketSwitchGuis implements IMessage {

    private GuiType guiType;

    public CPacketSwitchGuis() {}

    public CPacketSwitchGuis(GuiType guiType) {
        this.guiType = guiType;
        AEBaseGui.setSwitchingGuis(true);
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
            Container cont = player.openContainer;
            World w = player.worldObj;
            if (message.guiType == GuiType.BACKPACK_TERMINAL) {
                int s = Util.findBackPackTerminal(player);
                if (s != -1) {
                    InventoryHandler.openGui(player, w, new BlockPos(s, 0, 0), ForgeDirection.UNKNOWN, message.guiType);
                }
                return null;
            }
            if (cont instanceof AEBaseContainer c) {
                ContainerOpenContext context = ((AEBaseContainer) cont).getOpenContext();
                if (context == null) {
                    return null;
                }
                TileEntity te = context.getTile();
                if (te != null) {
                    InventoryHandler.openGui(
                        player,
                        player.worldObj,
                        new BlockPos(te),
                        Objects.requireNonNull(context.getSide()),
                        message.guiType);
                } else {
                    InventoryHandler.openGui(
                        player,
                        player.getEntityWorld(),
                        new BlockPos(((WirelessTerminal) (c.getTarget())).getInventorySlot(), 0, 0),
                        Objects.requireNonNull(context.getSide()),
                        message.guiType);
                }
            }
            return null;
        }

    }

}
