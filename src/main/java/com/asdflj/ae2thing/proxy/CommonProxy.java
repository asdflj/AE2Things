package com.asdflj.ae2thing.proxy;

import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.common.storage.StorageManager;
import com.asdflj.ae2thing.loader.ItemAndBlockHolder;
import com.asdflj.ae2thing.network.wrapper.AE2ThingNetworkWrapper;
import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.darkona.adventurebackpack.item.ItemAdventureBackpack;

import appeng.api.config.Upgrades;
import appeng.util.Platform;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ic2.core.Ic2Items;

public class CommonProxy {

    public AE2ThingNetworkWrapper netHandler = new AE2ThingNetworkWrapper(AE2Thing.MODID);;

    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance()
            .bus()
            .register(this);
        ModAndClassUtil.init();
    }

    @SubscribeEvent
    public void worldLoad(WorldEvent.Load event) {
        if (Platform.isServer() && event.world.provider.dimensionId == 0) {
            WorldSavedData w = event.world.mapStorage.loadData(StorageManager.class, AE2Thing.MODID);
            if (w == null) {
                StorageManager s = new StorageManager(AE2Thing.MODID);
                event.world.mapStorage.setData(AE2Thing.MODID, s);
            }
        }
    }

    public void init(FMLInitializationEvent event) {}

    public void postInit(FMLPostInitializationEvent event) {
        if (ModAndClassUtil.BACKPACK) {
            AE2ThingAPI.instance()
                .addBackpackItem(de.eydamos.backpack.item.ItemBackpackBase.class);
        }
        if (ModAndClassUtil.FTR) {
            AE2ThingAPI.instance()
                .addBackpackItem(forestry.storage.items.ItemBackpack.class);
        }
        if (ModAndClassUtil.ADVENTURE_BACKPACK) {
            AE2ThingAPI.instance()
                .addBackpackItem(ItemAdventureBackpack.class);
        }
        Upgrades.PATTERN_REFILLER.registerItem(ItemAndBlockHolder.INFUSION_PATTERN_TERMINAL.stack(), 1);
        if (ModAndClassUtil.IC2) {
            AE2ThingAPI.instance()
                .setDefaultFluidContainer(Ic2Items.cell);
        }
    }

    public void onLoadComplete(FMLLoadCompleteEvent event) {

    }

    public void serverStarting(FMLServerStartingEvent event) {

    }

    public void serverStopping(FMLServerStoppingEvent event) {
        StorageManager m = AE2ThingAPI.instance()
            .getStorageManager();
        if (m != null) m.setDirty(true);
    }
}
