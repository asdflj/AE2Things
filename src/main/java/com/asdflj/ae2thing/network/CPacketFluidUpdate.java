package com.asdflj.ae2thing.network;

import appeng.api.storage.data.IAEFluidStack;
import appeng.util.item.AEFluidStack;
import com.asdflj.ae2thing.client.gui.container.ContainerCraftingTerminal;
import com.glodblock.github.client.gui.container.ContainerFluidMonitor;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import java.io.IOException;

public class CPacketFluidUpdate implements IMessage {

    private IAEFluidStack fluid;
    private boolean shift;
    private int slotIndex;

    public CPacketFluidUpdate() {}

    /**
     * Used by transferStackInSlot. No clicked fluid, always shift.
     */
    public CPacketFluidUpdate(int slotIndex) {
        this(null, slotIndex, true);
    }

    /**
     * Used by handleMouseClick. Always uses mouse stack.
     */
    public CPacketFluidUpdate(IAEFluidStack fluid, boolean shift) {
        this(fluid, -1, shift);
    }

    private CPacketFluidUpdate(IAEFluidStack fluid, int slotIndex, boolean shift) {
        this.fluid = fluid;
        this.slotIndex = slotIndex;
        this.shift = shift;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            if (buf.readBoolean()) {
                this.fluid = AEFluidStack.loadFluidStackFromPacket(buf);
            } else {
                this.fluid = null;
            }
            this.slotIndex = buf.readInt();
            this.shift = buf.readBoolean();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        try {
            if (fluid == null) {
                buf.writeBoolean(false);
            } else {
                buf.writeBoolean(true);
                fluid.writeToPacket(buf);
            }
            buf.writeInt(this.slotIndex);
            buf.writeBoolean(this.shift);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class Handler implements IMessageHandler<CPacketFluidUpdate, IMessage> {

        @Override
        public IMessage onMessage(CPacketFluidUpdate message, MessageContext ctx) {
            Container container = ctx.getServerHandler().playerEntity.openContainer;
            EntityPlayer player = ctx.getServerHandler().playerEntity;
            if (container instanceof ContainerCraftingTerminal) {
                ((ContainerCraftingTerminal) container).postChange(message.fluid, player, message.slotIndex, message.shift);
            }
            return null;
        }
    }
}
