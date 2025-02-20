package com.asdflj.ae2thing.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.api.adapter.findit.IFindItAdapter;
import com.asdflj.ae2thing.util.StorageProvider;
import com.glodblock.github.util.Util;

import appeng.api.networking.IGrid;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.AEBaseContainer;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class CPacketFindCellItem implements IMessage {

    public boolean isFluid;
    public IAEItemStack item;

    public CPacketFindCellItem() {}

    public CPacketFindCellItem(ItemStack is) {
        this(is, Util.getFluidFromItem(is) != null);
    }

    public CPacketFindCellItem(ItemStack is, boolean isFluid) {
        this.item = AEItemStack.create(is);
        this.isFluid = isFluid;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.isFluid = buf.readBoolean();
        try {
            this.item = AEItemStack.loadItemStackFromPacket(buf);
        } catch (Exception ignored) {}

    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(isFluid);
        try {
            this.item.writeToPacket(buf);
        } catch (Exception ignored) {}
    }

    public static class Handler implements IMessageHandler<CPacketFindCellItem, IMessage> {

        @Override
        public IMessage onMessage(CPacketFindCellItem message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (player.openContainer instanceof AEBaseContainer container
                && container.getActionSource() instanceof PlayerSource ps) {
                List<StorageProvider> posList = new ArrayList<>();
                IGrid iGrid = null;
                try {
                    iGrid = ps.via.getActionableNode()
                        .getGrid();
                } catch (Exception ignored) {}
                if (iGrid == null) return null;
                Collection<IFindItAdapter> adapters = AE2ThingAPI.instance()
                    .getStorageProviders();
                for (IFindItAdapter adapter : adapters) {
                    posList.addAll(adapter.getStorageProviders(iGrid, message.item));
                }

                if (!posList.isEmpty()) AE2Thing.proxy.netHandler.sendTo(new SPacketFindCellItem(posList), player);
            }

            return null;
        }
    }
}
