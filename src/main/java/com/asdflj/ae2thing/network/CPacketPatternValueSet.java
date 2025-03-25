package com.asdflj.ae2thing.network;

import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;

import com.asdflj.ae2thing.client.gui.container.ContainerPatternValueAmount;
import com.asdflj.ae2thing.client.gui.container.IPatternValueContainer;
import com.asdflj.ae2thing.inventory.InventoryHandler;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.asdflj.ae2thing.util.BlockPos;
import com.asdflj.ae2thing.util.Util;
import com.glodblock.github.common.item.ItemFluidPacket;

import appeng.api.networking.IGridHost;
import appeng.container.ContainerOpenContext;
import appeng.container.interfaces.IInventorySlotAware;
import appeng.container.slot.SlotFake;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class CPacketPatternValueSet implements IMessage {

    private GuiType originGui;
    private int amount;
    private int valueIndex;

    public CPacketPatternValueSet() {
        // NO-OP
    }

    public CPacketPatternValueSet(int originalGui, int amount, int valueIndex) {
        this.originGui = GuiType.getByOrdinal(originalGui);
        this.amount = amount;
        this.valueIndex = valueIndex;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(originGui.ordinal());
        buf.writeInt(amount);
        buf.writeInt(valueIndex);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.originGui = GuiType.getByOrdinal(buf.readInt());
        this.amount = buf.readInt();
        this.valueIndex = buf.readInt();
    }

    public static class Handler implements IMessageHandler<CPacketPatternValueSet, IMessage> {

        @Override
        public IMessage onMessage(CPacketPatternValueSet message, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().playerEntity;
            if (player.openContainer instanceof ContainerPatternValueAmount cpv) {
                final Object target = cpv.getTarget();
                if (target instanceof IGridHost) {
                    final ContainerOpenContext context = cpv.getOpenContext();
                    if (context != null) {
                        final TileEntity te = context.getTile();
                        if (te != null) {
                            InventoryHandler.openGui(
                                player,
                                player.worldObj,
                                new BlockPos(te),
                                Objects.requireNonNull(context.getSide()),
                                message.originGui);
                        } else {
                            InventoryHandler.openGui(
                                player,
                                player.getEntityWorld(),
                                new BlockPos(((IInventorySlotAware) target).getInventorySlot(), 0, 0),
                                Objects.requireNonNull(context.getSide()),
                                message.originGui);
                        }
                        if (player.openContainer instanceof IPatternValueContainer) {
                            Slot slot = player.openContainer.getSlot(message.valueIndex);
                            if (slot instanceof SlotFake) {
                                ItemStack stack = slot.getStack()
                                    .copy();
                                if (Util.isFluidPacket(stack)) {
                                    FluidStack fluidStack = ItemFluidPacket.getFluidStack(stack);
                                    if (fluidStack != null) {
                                        fluidStack = Objects.requireNonNull(ItemFluidPacket.getFluidStack(stack))
                                            .copy();
                                        fluidStack.amount = message.amount;
                                    }
                                    slot.putStack(ItemFluidPacket.newStack(fluidStack));
                                } else {
                                    stack.stackSize = message.amount;
                                    slot.putStack(stack);
                                }
                            }
                        }
                    }
                }
            }
            return null;
        }
    }
}
