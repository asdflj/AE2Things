package com.asdflj.ae2thing.client;

import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;

import com.asdflj.ae2thing.client.textures.Texture;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ClientHelper {

    @SubscribeEvent
    public void updateTextureSheet(final TextureStitchEvent.Pre ev) {
        if (ev.map.getTextureType() == 0) {
            for (final Texture cb : Texture.values()) {
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
