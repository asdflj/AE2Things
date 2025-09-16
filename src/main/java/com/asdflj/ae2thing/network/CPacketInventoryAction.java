package com.asdflj.ae2thing.network;

import static com.asdflj.ae2thing.api.Constants.DISPLAY_ONLY;

import java.io.IOException;
import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.client.gui.container.ContainerCraftingTerminal;
import com.asdflj.ae2thing.client.gui.container.ContainerPatternValueAmount;
import com.asdflj.ae2thing.inventory.InventoryHandler;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.asdflj.ae2thing.inventory.item.WirelessTerminal;
import com.asdflj.ae2thing.util.BlockPos;
import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.crossmod.thaumcraft.AspectUtil;

import appeng.api.AEApi;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import appeng.container.implementations.ContainerCraftAmount;
import appeng.container.interfaces.IInventorySlotAware;
import appeng.helpers.InventoryAction;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.common.items.ItemCraftingAspect;

public class CPacketInventoryAction implements IMessage {

    private InventoryAction action;
    private int slot;
    private long id;
    private IAEItemStack stack;
    private boolean isEmpty;

    public CPacketInventoryAction() {}

    public CPacketInventoryAction(final InventoryAction action, final int slot, final int id) {
        this.action = action;
        this.slot = slot;
        this.id = id;
        this.stack = null;
        this.isEmpty = true;
    }

    public CPacketInventoryAction(final InventoryAction action, final int slot, final int id, IAEItemStack stack) {
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
        action = InventoryAction.values()[buf.readInt()];
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

    public static class Handler implements IMessageHandler<CPacketInventoryAction, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(CPacketInventoryAction message, MessageContext ctx) {
            final EntityPlayerMP sender = ctx.getServerHandler().playerEntity;
            if(sender.openContainer instanceof ContainerCraftingTerminal) {
                return null;
            }
            if (sender.openContainer instanceof final AEBaseContainer baseContainer) {
                Object target = baseContainer.getTarget();
                if (message.action == InventoryAction.AUTO_CRAFT) {
                    final ContainerOpenContext context = baseContainer.getOpenContext();
                    if (context != null) {
                        final TileEntity te = context.getTile();
                        if (te != null || target instanceof WirelessTerminal) {
                            if (message.stack == null){
                                message.stack = baseContainer.getTargetStack();
                            }
                            if(message.stack.getItem() instanceof ItemFluidDrop){
                                IAEFluidStack fs = ItemFluidDrop.getAeFluidStack(message.stack);
                                if(ModAndClassUtil.THE &&AspectUtil.isEssentiaGas(fs)){
                                    Aspect aspect = AspectUtil.getAspectFromGas(fs.getFluidStack());
                                    IAEItemStack result = AEApi.instance().storage()
                                        .createItemStack(ItemCraftingAspect.createStackForAspect(aspect, 1));
                                    baseContainer.setTargetStack(result);
                                }else{
                                    ItemStack is = message.stack.getItemStack().copy();
                                    NBTTagCompound data = is.getTagCompound();
                                    data.removeTag(DISPLAY_ONLY);
                                    is.setTagCompound(data);
                                    baseContainer.setTargetStack(AEItemStack.create(is));
                                }
                            }else{
                                baseContainer.setTargetStack(message.stack);
                            }
                            if(te != null){
                                InventoryHandler.openGui(
                                    sender,
                                    te.getWorldObj(),
                                    new BlockPos(te),
                                    Objects.requireNonNull(baseContainer.getOpenContext().getSide()),
                                    GuiType.CRAFTING_AMOUNT);
                            }else{
                                InventoryHandler.openGui(
                                    sender,
                                    sender.getEntityWorld(),
                                    new BlockPos(((WirelessTerminal) target).getInventorySlot(),0,0),
                                    Objects.requireNonNull(baseContainer.getOpenContext().getSide()),
                                    GuiType.CRAFTING_AMOUNT_ITEM);
                            }
                        }
                        if (sender.openContainer instanceof final ContainerCraftAmount cca) {
                            if (baseContainer.getTargetStack() != null) {
                                cca.getCraftingItem().putStack(baseContainer.getTargetStack().getItemStack());
                                cca.setItemToCraft(baseContainer.getTargetStack());
                            }
                            cca.detectAndSendChanges();
                        }
                    }
                } else if (message.action == InventoryAction.SET_PATTERN_VALUE) {
                    final ContainerOpenContext context = baseContainer.getOpenContext();
                    if (context != null && message.stack != null) {
                        final TileEntity te = context.getTile();
                        if (te != null) {
                            InventoryHandler.openGui(
                                    sender,
                                    te.getWorldObj(),
                                    new BlockPos(te),
                                    Objects.requireNonNull(baseContainer.getOpenContext().getSide()),
                                    GuiType.PATTERN_VALUE_SET);
                        }else{
                            InventoryHandler.openGui(
                                sender,
                                sender.getEntityWorld(),
                                new BlockPos(((IInventorySlotAware)target).getInventorySlot(),0,0),
                                Objects.requireNonNull(baseContainer.getOpenContext().getSide()),
                                GuiType.PATTERN_VALUE_SET_ITEM);
                        }
                        int amt = (int) message.stack.getStackSize();
                        AE2Thing.proxy.netHandler.sendTo(new SPacketSetItemAmount(amt), sender);
                        if (sender.openContainer instanceof final ContainerPatternValueAmount cpv) {
                            if (baseContainer.getTargetStack() != null) {
                                cpv.setValueIndex(message.slot);
                                cpv.getPatternValue().putStack(baseContainer.getTargetStack().getItemStack());
                            }
                            cpv.detectAndSendChanges();
                        }
                    }
                } else {
                    baseContainer.doAction(sender, message.action, message.slot, message.id);
                }
            }
            return null;
        }
    }
}
