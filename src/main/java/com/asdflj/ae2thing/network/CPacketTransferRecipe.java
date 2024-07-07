package com.asdflj.ae2thing.network;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import com.asdflj.ae2thing.client.gui.container.ContainerInfusionPatternTerminal;
import com.asdflj.ae2thing.client.gui.container.ContainerMonitor;
import com.asdflj.ae2thing.nei.object.OrderStack;
import com.glodblock.github.common.item.ItemFluidPacket;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class CPacketTransferRecipe implements IMessage {

    private List<OrderStack<?>> inputs;
    private List<OrderStack<?>> outputs;
    private boolean isCraft;
    private static final int MAX_INDEX = 32;
    private boolean shift;

    public CPacketTransferRecipe() {}

    public CPacketTransferRecipe(List<OrderStack<?>> IN, List<OrderStack<?>> OUT, boolean craft) {
        this(IN, OUT, craft, false);
    }

    public CPacketTransferRecipe(List<OrderStack<?>> IN, List<OrderStack<?>> OUT, boolean craft, boolean shift) {
        this.inputs = IN;
        this.outputs = OUT;
        this.isCraft = craft;
        this.shift = shift;
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
    }

    public static class Handler implements IMessageHandler<CPacketTransferRecipe, IMessage> {

        @Nullable
        @Override
        @SuppressWarnings("unchecked")
        public IMessage onMessage(CPacketTransferRecipe message, MessageContext ctx) {
            Container c = ctx.getServerHandler().playerEntity.openContainer;
            if (c instanceof ContainerMonitor) {
                if (c instanceof ContainerInfusionPatternTerminal ct) {
                    // pattern terminal only

                    ct.getPatternTerminal()
                        .setCraftingRecipe(true);
                    ct.setCrafting(true);
                    IInventory inputSlot = ct.getInventoryByName("crafting");
                    IInventory outputSlot = ct.getInventoryByName("output");
                    for (int i = 0; i < inputSlot.getSizeInventory(); i++) {
                        inputSlot.setInventorySlotContents(i, null);
                    }
                    for (int i = 0; i < outputSlot.getSizeInventory(); i++) {
                        outputSlot.setInventorySlotContents(i, null);
                    }
                    transferPack(message.inputs, inputSlot);
                    transferPack(message.outputs, outputSlot);
                    c.onCraftMatrixChanged(inputSlot);
                    c.onCraftMatrixChanged(outputSlot);
                }
            }

            return null;
        }

        private void transferPack(List<OrderStack<?>> packs, IInventory inv) {
            for (OrderStack<?> stack : packs) {
                if (stack != null) {
                    int index = stack.getIndex();
                    ItemStack stack1;
                    if (stack.getStack() instanceof ItemStack) {
                        stack1 = ((ItemStack) stack.getStack()).copy();
                    } else if (stack.getStack() instanceof FluidStack) {
                        stack1 = ItemFluidPacket.newStack((FluidStack) stack.getStack());
                    } else throw new UnsupportedOperationException("Trying to get an unsupported item!");
                    if (index < inv.getSizeInventory()) inv.setInventorySlotContents(index, stack1);
                }
            }
        }
    }
}
