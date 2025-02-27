package com.asdflj.ae2thing.network;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;

import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.api.adapter.pattern.IPatternTerminalAdapter;
import com.asdflj.ae2thing.nei.object.OrderStack;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class CPacketTransferRecipe implements IMessage {

    private List<OrderStack<?>> inputs;
    private List<OrderStack<?>> outputs;
    private String identifier;
    public boolean isCraft;
    private static final int MAX_INDEX = 32;
    public boolean shift;

    public CPacketTransferRecipe() {}

    public CPacketTransferRecipe(List<OrderStack<?>> IN, List<OrderStack<?>> OUT, boolean craft) {
        this(IN, OUT, craft, false);
    }

    public CPacketTransferRecipe(List<OrderStack<?>> IN, List<OrderStack<?>> OUT, boolean craft, boolean shift) {
        this(IN, OUT, craft, shift, Constants.NEI_DEFAULT);
    }

    public CPacketTransferRecipe(List<OrderStack<?>> IN, List<OrderStack<?>> OUT, boolean craft, boolean shift,
        String identifier) {
        this.inputs = IN;
        this.outputs = OUT;
        this.isCraft = craft;
        this.shift = shift;
        this.identifier = identifier;
    }

    // TODO: this should use GZIP to compress the message
    // NBT to ByteBuf has a compress stream
    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(isCraft);
        buf.writeBoolean(shift);
        NBTTagCompound nbt_m = new NBTTagCompound();
        NBTTagCompound nbt_i = new NBTTagCompound();
        NBTTagCompound nbt_o = new NBTTagCompound();
        for (OrderStack<?> stack : inputs) {
            stack.writeToNBT(nbt_i);
        }
        for (OrderStack<?> stack : outputs) {
            stack.writeToNBT(nbt_o);
        }
        nbt_m.setTag("i", nbt_i);
        nbt_m.setTag("o", nbt_o);
        ByteBufUtils.writeTag(buf, nbt_m);
        ByteBufUtils.writeUTF8String(buf, this.identifier);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        isCraft = buf.readBoolean();
        shift = buf.readBoolean();
        inputs = new LinkedList<>();
        outputs = new LinkedList<>();
        NBTTagCompound nbt_m = ByteBufUtils.readTag(buf);
        NBTTagCompound nbt_i = nbt_m.getCompoundTag("i");
        NBTTagCompound nbt_o = nbt_m.getCompoundTag("o");
        for (int i = 0; i < MAX_INDEX; i++) {
            OrderStack<?> tmp = OrderStack.readFromNBT(nbt_i, null, i);
            if (tmp != null) inputs.add(tmp);
        }
        for (int i = 0; i < MAX_INDEX; i++) {
            OrderStack<?> tmp = OrderStack.readFromNBT(nbt_o, null, i);
            if (tmp != null) outputs.add(tmp);
        }
        identifier = ByteBufUtils.readUTF8String(buf);
    }

    public static class Handler implements IMessageHandler<CPacketTransferRecipe, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(CPacketTransferRecipe message, MessageContext ctx) {
            Container c = ctx.getServerHandler().playerEntity.openContainer;
            IPatternTerminalAdapter adapter = AE2ThingAPI.instance()
                .terminal()
                .getPatternTerminal(c);
            if (adapter != null && adapter.getIdentifiers()
                .containsKey(message.identifier)) {
                adapter.transfer(c, message.inputs, message.outputs, message.identifier, message);
                return null;
            }
            return null;
        }
    }
}
