package com.asdflj.ae2thing.client;

import java.util.Arrays;

import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;

import com.asdflj.ae2thing.client.icon.Fluids;
import com.asdflj.ae2thing.client.textures.BlockTexture;
import com.asdflj.ae2thing.client.textures.ItemTexture;
import com.asdflj.ae2thing.util.NameConst;

import appeng.api.util.AEColor;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ClientHelper {

    @SubscribeEvent
    public void updateTextureSheet(final TextureStitchEvent.Pre ev) {
        if (ev.map.getTextureType() == 0) {
            for (int i = 0; i < AEColor.values().length; i++) {
                BlockTexture.registerIcon(
                    ev.map,
                    NameConst.BLOCK_WIRELESS_DISTRIBUTOR,
                    NameConst.RES_KEY + "wireless_distributor/side_on" + i);
                BlockTexture.registerIcon(
                    ev.map,
                    NameConst.BLOCK_WIRELESS_DISTRIBUTOR,
                    NameConst.RES_KEY + "wireless_distributor/side_off" + i,
                    false);
            }

            for (final BlockTexture cb : BlockTexture.values()) {
                cb.registerIcon(ev.map);
            }
            Arrays.stream(Fluids.values())
                .forEach(fluids -> fluids.registerIcon(ev.map));
        }
        if (ev.map.getTextureType() == 1) {
            for (final ItemTexture cb : ItemTexture.values()) {
                cb.registerIcon(ev.map);
            }
        }
    }

    public static void register() {
        ClientHelper handler = new ClientHelper();
        MinecraftForge.EVENT_BUS.register(handler);
        FMLCommonHandler.instance()
            .bus()
            .register(handler);
    }
}
