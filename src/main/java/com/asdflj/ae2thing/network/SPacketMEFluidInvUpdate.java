package com.asdflj.ae2thing.network;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import com.asdflj.ae2thing.client.gui.IGuiMonitorTerminal;

import appeng.api.storage.data.IAEFluidStack;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/**
 * Dedicated packet for fluid updates.
 */
public class SPacketMEFluidInvUpdate extends SPacketMEBaseInvUpdate implements IMessage {

    public SPacketMEFluidInvUpdate() {}

    public void addAll(final List<IAEFluidStack> list) {
        this.list.addAll(list);
    }

    public void appendFluid(final IAEFluidStack is) {
        this.list.add(is);
    }

    public static class Handler implements IMessageHandler<SPacketMEFluidInvUpdate, IMessage> {

        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public IMessage onMessage(SPacketMEFluidInvUpdate message, MessageContext ctx) {
            final GuiScreen gs = Minecraft.getMinecraft().currentScreen;
            if (gs instanceof IGuiMonitorTerminal gpt) {
                gpt.postFluidUpdate((List) message.list);
            }
            return null;
        }
    }
}
