package com.asdflj.ae2thing.network;

import java.io.IOException;
import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.client.gui.container.ContainerCraftingTerminal;
import com.asdflj.ae2thing.client.gui.container.ContainerPatternValueName;
import com.asdflj.ae2thing.inventory.InventoryHandler;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.asdflj.ae2thing.inventory.item.WirelessTerminal;
import com.asdflj.ae2thing.util.BlockPos;
import com.asdflj.ae2thing.util.InventoryActionExtend;

import appeng.api.storage.data.IAEItemStack;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class CPacketInventoryActionExtend implements IMessage {

    private InventoryActionExtend action;
    private int slot;
    private long id;
    private IAEItemStack stack;
    private boolean isEmpty;

    public CPacketInventoryActionExtend() {}

    public CPacketInventoryActionExtend(final InventoryActionExtend action, final int slot, final int id) {
        this.action = action;
        this.slot = slot;
        this.id = id;
        this.stack = null;
        this.isEmpty = true;
    }

    public CPacketInventoryActionExtend(final InventoryActionExtend action, final int slot, final int id,
        IAEItemStack stack) {
        this.action = action;
        this.slot = slot;
        this.id = id;
        this.stack = stack;
        this.isEmpty = stack == null;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(action.ordinal());
        buf.writeInt(slot);
        buf.writeLong(id);
        buf.writeBoolean(isEmpty);
        if (!isEmpty) {
            try {
                stack.writeToPacket(buf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        action = InventoryActionExtend.values()[buf.readInt()];
        slot = buf.readInt();
        id = buf.readLong();
        isEmpty = buf.readBoolean();
        if (!isEmpty) {
            try {
                stack = AEItemStack.loadItemStackFromPacket(buf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class Handler implements IMessageHandler<CPacketInventoryActionExtend, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(CPacketInventoryActionExtend message, MessageContext ctx) {
            final EntityPlayerMP sender = ctx.getServerHandler().playerEntity;
            if(sender.openContainer instanceof ContainerCraftingTerminal) {
                return null;
            }
            if (sender.openContainer instanceof final AEBaseContainer baseContainer) {
                Object target = baseContainer.getTarget();
                if (message.action == InventoryActionExtend.SET_PATTERN_NAME) {
                    final ContainerOpenContext context = baseContainer.getOpenContext();
                    if (context != null && message.stack != null) {
                        final TileEntity te = context.getTile();
                        if (te != null) {
                            InventoryHandler.openGui(
                                    sender,
                                    te.getWorldObj(),
                                    new BlockPos(te),
                                    Objects.requireNonNull(baseContainer.getOpenContext().getSide()),
                                    GuiType.PATTERN_NAME_SET);
                        }else{
                            InventoryHandler.openGui(
                                sender,
                                sender.getEntityWorld(),
                                new BlockPos(((WirelessTerminal) target).getInventorySlot(),0,0),
                                Objects.requireNonNull(baseContainer.getOpenContext().getSide()),
                                GuiType.PATTERN_NAME_SET_ITEM);
                        }

                        ItemStack itemStack = message.stack.getItemStack();
                        if(itemStack.hasDisplayName()){
                            String name = itemStack.getDisplayName();
                            AE2Thing.proxy.netHandler.sendTo(new SPacketSetItemName(name), sender);
                        }
                        if (sender.openContainer instanceof final ContainerPatternValueName cpv) {
                            if (baseContainer.getTargetStack() != null) {
                                cpv.setValueIndex(message.slot);
                                cpv.getPatternValue().putStack(baseContainer.getTargetStack().getItemStack());
                            }
                            cpv.detectAndSendChanges();
                        }
                    }
                }
            }
            return null;
        }
    }
}
