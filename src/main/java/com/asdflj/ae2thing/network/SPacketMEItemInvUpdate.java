package com.asdflj.ae2thing.network;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.client.gui.GuiCraftingTerminal;
import com.asdflj.ae2thing.client.gui.GuiItemMonitor;

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
            if (gs instanceof GuiItemMonitor gim) {
                if (message.ref == 0) {
                    gim.postUpdate((List) message.list);
                } else if (message.ref == 1) {
                    if (gs instanceof GuiCraftingTerminal gct) {
                        ItemStack is = null;
                        if (!message.isEmpty()) {
                            is = ((IAEItemStack) message.list.get(0)).getItemStack();
                        }
                        gct.setPlayerInv(is);
                    }
                }
            }
            return null;
        }
    }
}
