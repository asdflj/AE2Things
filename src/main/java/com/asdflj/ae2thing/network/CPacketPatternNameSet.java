package com.asdflj.ae2thing.network;

import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import com.asdflj.ae2thing.client.gui.container.ContainerPatternValueName;
import com.asdflj.ae2thing.client.gui.container.widget.IWidgetPatternContainer;
import com.asdflj.ae2thing.inventory.InventoryHandler;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.asdflj.ae2thing.inventory.item.WirelessTerminal;
import com.asdflj.ae2thing.util.BlockPos;

import appeng.api.networking.IGridHost;
import appeng.container.ContainerOpenContext;
import appeng.container.slot.SlotFake;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class CPacketPatternNameSet implements IMessage {

    private GuiType originGui;
    private String name;
    private int valueIndex;

    public CPacketPatternNameSet() {
        // NO-OP
    }

    public CPacketPatternNameSet(GuiType originalGui, String name, int valueIndex) {
        this.originGui = originalGui;
        this.name = name;
        this.valueIndex = valueIndex;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(originGui.ordinal());
        ByteBufUtils.writeUTF8String(buf, name);
        buf.writeInt(valueIndex);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.originGui = GuiType.getByOrdinal(buf.readInt());
        this.name = ByteBufUtils.readUTF8String(buf);
        this.valueIndex = buf.readInt();
    }

    public static class Handler implements IMessageHandler<CPacketPatternNameSet, IMessage> {

        @Override
        public IMessage onMessage(CPacketPatternNameSet message, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().playerEntity;
            if (player.openContainer instanceof ContainerPatternValueName cpv) {
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
                                new BlockPos(((WirelessTerminal) target).getInventorySlot(), 0, 0),
                                Objects.requireNonNull(context.getSide()),
                                message.originGui);
                        }
                        if (player.openContainer instanceof IWidgetPatternContainer) {
                            SetName(message, player);
                        }
                    }
                }
            }
            return null;
        }

        private void SetName(CPacketPatternNameSet message, EntityPlayer player) {
            Slot slot = player.openContainer.getSlot(message.valueIndex);
            if (slot instanceof SlotFake) {
                ItemStack stack = slot.getStack()
                    .copy();
                if (message.name.isEmpty()) {
                    stack.func_135074_t();
                } else {
                    stack.setStackDisplayName(message.name);
                }
                slot.putStack(stack);
            }
        }
    }
}
