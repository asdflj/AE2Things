package com.asdflj.ae2thing.network;

import net.minecraft.entity.player.EntityPlayerMP;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.Constants;

import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.security.IActionHost;
import appeng.container.AEBaseContainer;
import appeng.container.implementations.ContainerWirelessTerm;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class CPacketNetworkCraftingItems implements IMessage {

    public CPacketNetworkCraftingItems() {}

    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    public static class Handler implements IMessageHandler<CPacketNetworkCraftingItems, IMessage> {

        @Override
        public IMessage onMessage(CPacketNetworkCraftingItems message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            try {
                if (player.openContainer instanceof AEBaseContainer c && c.getTarget() instanceof IActionHost host) {
                    sendToClient(host, player);
                } else if (player.openContainer instanceof ContainerWirelessTerm o
                    && o.getWirelessTerminalGUIObject() != null) {
                        // for ae wireless terminal
                        sendToClient(o.getWirelessTerminalGUIObject(), player);
                    }
            } catch (Exception ignored) {}
            return null;
        }

        private void sendToClient(IActionHost host, EntityPlayerMP player) {
            ICraftingGrid craftingGrid = host.getActionableNode()
                .getGrid()
                .getCache(ICraftingGrid.class);
            SPacketMEItemInvUpdate piu = new SPacketMEItemInvUpdate(Constants.MessageType.UPDATE_PINNED_ITEMS);
            for (ICraftingCPU cpu : craftingGrid.getCpus()) {
                if (cpu.getFinalOutput() == null || !cpu.isBusy()) continue;
                piu.appendItem(cpu.getFinalOutput());
            }
            AE2Thing.proxy.netHandler.sendTo(piu, player);
        }
    }
}
