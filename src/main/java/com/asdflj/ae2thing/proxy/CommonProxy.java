package com.asdflj.ae2thing.proxy;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.loader.Config;
import com.asdflj.ae2thing.network.wrapper.AE2ThingNetworkWrapper;
import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.darkona.adventurebackpack.item.ItemAdventureBackpack;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

public class CommonProxy {

    public AE2ThingNetworkWrapper netHandler = new AE2ThingNetworkWrapper(AE2Thing.MODID);;

    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());
    }

    public void init(FMLInitializationEvent event) {}

    public void postInit(FMLPostInitializationEvent event) {
        ModAndClassUtil.init();
        if (ModAndClassUtil.BACKPACK) {
            AE2ThingAPI.instance()
                .addBackpackItem(de.eydamos.backpack.item.ItemBackpack.class);
        }
        if (ModAndClassUtil.FTR) {
            AE2ThingAPI.instance()
                .addBackpackItem(forestry.storage.items.ItemBackpack.class);
        }
        if (ModAndClassUtil.ADVENTURE_BACKPACK) {
            AE2ThingAPI.instance()
                .addBackpackItem(ItemAdventureBackpack.class);
        }
    }

    public void onLoadComplete(FMLLoadCompleteEvent event) {}

    public void serverStarting(FMLServerStartingEvent event) {}
}
