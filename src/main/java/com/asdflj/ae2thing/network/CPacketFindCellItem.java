package com.asdflj.ae2thing.network;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.util.CellPos;
import com.glodblock.github.util.Util;

import appeng.api.AEApi;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.DimensionalCoord;
import appeng.container.AEBaseContainer;
import appeng.tile.storage.TileChest;
import appeng.tile.storage.TileDrive;
import appeng.util.item.AEFluidStack;
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
                List<CellPos> posList = new ArrayList<>();
                for (IGridNode node : ps.via.getActionableNode()
                    .getGrid()
                    .getMachines(TileDrive.class)) {
                    if (node.getMachine() instanceof TileDrive drive) {
                        for (int i = 0; i < drive.getCellCount(); i++) {
                            ItemStack is = drive.getStackInSlot(i);
                            if (findStack(is, message.isFluid, message.item)) {
                                posList.add(new CellPos(new DimensionalCoord(drive), i));
                            }
                        }
                    }
                }
                for (IGridNode node : ps.via.getActionableNode()
                    .getGrid()
                    .getMachines(TileChest.class)) {
                    if (node.getMachine() instanceof TileChest chest) {
                        ItemStack cell = chest.getInternalInventory()
                            .getStackInSlot(1);
                        if (findStack(cell, message.isFluid, message.item)) {
                            posList.add(new CellPos(new DimensionalCoord(chest), 1));
                        }
                    }
                }
                if (!posList.isEmpty()) AE2Thing.proxy.netHandler.sendTo(new SPacketFindCellItem(posList), player);
            }

            return null;
        }

        private boolean findStack(ItemStack cell, boolean isFluid, IAEItemStack request) {
            IMEInventory inv = getInv(cell, isFluid ? StorageChannel.FLUIDS : StorageChannel.ITEMS);
            if (inv == null) return false;
            boolean result;
            if (isFluid) {
                FluidStack fs = Util.getFluidFromItem(request.getItemStack());
                result = inv.getAvailableItem(AEFluidStack.create(fs)) != null;
            } else {
                result = inv.getAvailableItem(request) != null;
            }
            return result;
        }

        private IMEInventory getInv(final ItemStack is, StorageChannel channel) {
            if (channel == StorageChannel.ITEMS) {
                return AEApi.instance()
                    .registries()
                    .cell()
                    .getCellInventory(is, null, StorageChannel.ITEMS);
            } else if (channel == StorageChannel.FLUIDS) {
                return AEApi.instance()
                    .registries()
                    .cell()
                    .getCellInventory(is, null, StorageChannel.FLUIDS);
            }
            return null;
        }
    }
}
