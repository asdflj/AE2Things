package com.asdflj.ae2thing.network;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.client.gui.IGuiMonitorTerminal;
import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.asdflj.ae2thing.util.TheUtil;

import appeng.api.storage.data.IAEItemStack;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/**
 * Packet dedicated to item inventory update.
 */
public class SPacketMEItemInvUpdate extends SPacketMEBaseInvUpdate implements IMessage {

    public SPacketMEItemInvUpdate() {
        super();
    }

    /**
     * Used for the GUI to confirm crafting. 0 = available 1 = pending 2 = missing
     */
    public SPacketMEItemInvUpdate(byte b) {
        super(b);
    }

    public SPacketMEItemInvUpdate(Constants.MessageType type) {
        this(type.type);
    }

    public void appendItem(final IAEItemStack is) {
        list.add(is);
    }

    public void addAll(final List<IAEItemStack> list) {
        this.list.addAll(list);
    }

    public static class Handler implements IMessageHandler<SPacketMEItemInvUpdate, IMessage> {

        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public IMessage onMessage(SPacketMEItemInvUpdate message, MessageContext ctx) {
            final GuiScreen gs = Minecraft.getMinecraft().currentScreen;
            if (message.ref == Constants.MessageType.UPDATE_ITEMS.type && gs instanceof IGuiMonitorTerminal gmt) {
                gmt.postUpdate((List) message.list);
            } else if (message.ref == Constants.MessageType.UPDATE_PLAYER_ITEM.type
                && gs instanceof IGuiMonitorTerminal gmt) {
                    ItemStack is = null;
                    if (!message.isEmpty()) {
                        is = ((IAEItemStack) message.list.get(0)).getItemStack();
                    }
                    gmt.setPlayerInv(is);
                } else if (message.ref == Constants.MessageType.UPDATE_PLAYER_CURRENT_ITEM.type) {
                    if (gs == null) {
                        Minecraft mc = Minecraft.getMinecraft();
                        EntityClientPlayerMP player = mc.thePlayer;
                        IAEItemStack is = (IAEItemStack) message.list.get(0);
                        if (is == null) return null;
                        player.inventory.setInventorySlotContents(player.inventory.currentItem, is.getItemStack());
                    }
                } else if (message.ref == Constants.MessageType.UPDATE_PINNED_ITEMS.type) {
                    AE2ThingAPI.instance()
                        .getPinned()
                        .updatePinnedItems(ItemCraftingAspect2FluidDrop((List) message.list));
                } else if (message.ref == Constants.MessageType.ADD_PINNED_ITEM.type) {
                    if (!message.isEmpty()) {
                        AE2ThingAPI.instance()
                            .getPinned()
                            .add(((IAEItemStack) message.list.get(0)));
                    }
                } else if (message.ref == Constants.MessageType.NOTIFICATION.type) {
                    if (!message.isEmpty()) {
                        AE2ThingAPI.instance()
                            .addCraftingCompleteNotification(((IAEItemStack) message.list.get(0)));
                    }
                }
            return null;
        }

        private static List<IAEItemStack> ItemCraftingAspect2FluidDrop(List<IAEItemStack> items) {
            if (!ModAndClassUtil.THE) {
                return new ArrayList<>(items);
            }
            List<IAEItemStack> list = new ArrayList<>();
            for (IAEItemStack item : items) {
                if (TheUtil.isItemCraftingAspect(item)) {
                    list.add(TheUtil.itemCraftingAspect2FluidDrop(item));
                } else {
                    list.add(item);
                }
            }
            return list;
        }
    }
}
