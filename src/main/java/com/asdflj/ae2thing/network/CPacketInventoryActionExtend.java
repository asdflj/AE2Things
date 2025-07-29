package com.asdflj.ae2thing.network;

import static appeng.api.networking.crafting.CraftingItemList.ACTIVE;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.api.InventoryActionExtend;
import com.asdflj.ae2thing.api.WirelessObject;
import com.asdflj.ae2thing.client.gui.container.ContainerCraftingTerminal;
import com.asdflj.ae2thing.client.gui.container.ContainerPatternModifier;
import com.asdflj.ae2thing.client.gui.container.ContainerPatternValueName;
import com.asdflj.ae2thing.inventory.InventoryHandler;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.asdflj.ae2thing.inventory.item.WirelessTerminal;
import com.asdflj.ae2thing.util.BlockPos;
import com.asdflj.ae2thing.util.CPUCraftingPreview;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import appeng.core.localization.GuiText;
import appeng.me.cluster.implementations.CraftingCPUCluster;
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
        this(action, slot, id, null);
    }

    public CPacketInventoryActionExtend(final InventoryActionExtend action) {
        this(action, 0, 0, null);
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

        private void extractItemFromME(EntityPlayer player, IAEItemStack requestItem, int slot) {
            if (requestItem.getStackSize() <= 0) {
                return;
            }
            for (int i = 0; i < player.inventory.mainInventory.length; i++) {
                ItemStack item = player.inventory.mainInventory[i];
                if (item != null && item.getItem() instanceof IWirelessTermHandler) {
                    try {
                        WirelessObject object = new WirelessObject(item, player.worldObj, slot, 0, 0, player);
                        if (object.rangeCheck() && requestItem.getStackSize() > 0) {
                            IAEItemStack result = object.getItemInventory()
                                .extractItems(requestItem, Actionable.MODULATE, object.getSource());
                            if (result != null) {
                                requestItem.decStackSize(result.getStackSize());
                            }
                            if (requestItem.getStackSize() <= 0) {
                                break;
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }
        }

        @Nullable
        @Override
        public IMessage onMessage(CPacketInventoryActionExtend message, MessageContext ctx) {
            final EntityPlayerMP sender = ctx.getServerHandler().playerEntity;
            if(message.action == InventoryActionExtend.REQUEST_ITEM && sender.inventory.mainInventory[message.slot] == null){
                message.stack.setStackSize(message.stack.getItemStack().getMaxStackSize());
                IAEItemStack requestItem = message.stack.copy();
                extractItemFromME(sender,requestItem,message.slot);
                message.stack.decStackSize(requestItem.getStackSize());
                if(message.stack.getStackSize() > 0){
                    sender.inventory.setInventorySlotContents(message.slot,message.stack.getItemStack());
                }
                return null;
            }
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
                } else if (message.action == InventoryActionExtend.GET_CRAFTING_STATE) {
                    if(target instanceof IActionHost gh){
                        ICraftingGrid craftingGrid = gh.getActionableNode()
                            .getGrid()
                            .getCache(ICraftingGrid.class);
                        NBTTagCompound cpuData = new NBTTagCompound();
                        NBTTagList tagList = new NBTTagList();
                        cpuData.setTag(Constants.CPU_LIST,tagList);
                        int i = 0;
                        for (ICraftingCPU cpu: craftingGrid.getCpus()) {
                            i++;
                            if(cpu instanceof CraftingCPUCluster ccc && ccc.getFinalOutput() != null){
                                if(message.stack.hashCode() == ccc.getFinalOutput().hashCode()){
                                    IItemList<IAEItemStack> list =  AEApi.instance().storage().createPrimitiveItemList();
                                    ccc.getListOfItem(list,ACTIVE);
                                    List<IAEItemStack> activeItems = Arrays.stream(list.toArray(list.toArray(new IAEItemStack[0]))).limit(CPUCraftingPreview.maxSize).sorted(Comparator.comparingLong(IAEItemStack::getStackSize).reversed()).collect(Collectors.toList());
                                    if(activeItems.isEmpty()){
                                        continue;
                                    }
                                    NBTTagCompound data = new NBTTagCompound();
                                    final String name;
                                    if(ccc.getName().isEmpty()){
                                        name = GuiText.CPUs.getLocal() + ": #" + i;
                                    }else{
                                        name =GuiText.CPUs.getLocal() + ": "  + ccc.getName().substring(0, Math.min(20, ccc.getName().length()));
                                    }
                                    new CPUCraftingPreview(name, ccc.getRemainingItemCount(),ccc.getElapsedTime(),  activeItems).writeToNBT(data);
                                    tagList.appendTag(data);
                                }
                            }
                        }
                        AE2Thing.proxy.netHandler.sendTo(new SPacketCraftingStateUpdate(cpuData),ctx.getServerHandler().playerEntity);
                    }
                } else if (message.action == InventoryActionExtend.CLEAR_PATTERN && baseContainer instanceof ContainerPatternModifier patternModifier) {
                    patternModifier.clearPattern();
                } else if (message.action == InventoryActionExtend.REPLACE_PATTERN && baseContainer instanceof ContainerPatternModifier patternModifier) {
                    patternModifier.replacePattern();
                }
            }
            return null;
        }
    }

}
