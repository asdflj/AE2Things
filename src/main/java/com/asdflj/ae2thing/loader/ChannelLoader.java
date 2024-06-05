package com.asdflj.ae2thing.loader;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.world.World;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.network.CPacketNEIRecipe;
import com.asdflj.ae2thing.network.CPacketSwitchGuis;
import com.asdflj.ae2thing.network.SPacketMEItemInvUpdate;
import com.asdflj.ae2thing.network.wrapper.AE2ThingNetworkWrapper;

import cpw.mods.fml.relauncher.Side;

public class ChannelLoader implements Runnable {

    public static final ChannelLoader INSTANCE = new ChannelLoader();

    @Override
    public void run() {
        int id = 0;
        AE2ThingNetworkWrapper netHandler = AE2Thing.proxy.netHandler;
        netHandler
            .registerMessage(new SPacketMEItemInvUpdate.Handler(), SPacketMEItemInvUpdate.class, id++, Side.CLIENT);
        netHandler.registerMessage(new CPacketNEIRecipe.Handler(), CPacketNEIRecipe.class, id++, Side.SERVER);
        netHandler.registerMessage(new CPacketSwitchGuis.Handler(), CPacketSwitchGuis.class, id++, Side.SERVER);
    }

    public static void sendPacketToAllPlayers(Packet packet, World world) {
        for (Object player : world.playerEntities) {
            if (player instanceof EntityPlayerMP) {
                ((EntityPlayerMP) player).playerNetServerHandler.sendPacket(packet);
            }
        }
    }
}
