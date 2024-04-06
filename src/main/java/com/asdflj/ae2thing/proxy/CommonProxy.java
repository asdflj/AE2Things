package com.asdflj.ae2thing.proxy;

import java.util.List;
import java.util.Map;

import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.common.storage.StorageManager;
import com.asdflj.ae2thing.loader.Config;
import com.asdflj.ae2thing.network.wrapper.AE2ThingNetworkWrapper;
import com.asdflj.ae2thing.util.Ae2Reflect;
import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.darkona.adventurebackpack.item.ItemAdventureBackpack;

import appeng.util.Platform;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class CommonProxy {

    public AE2ThingNetworkWrapper netHandler = new AE2ThingNetworkWrapper(AE2Thing.MODID);;

    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance()
            .bus()
            .register(this);
    }

    @SubscribeEvent
    public void worldLoad(WorldEvent.Load event) {
        if (Platform.isServer() && event.world.provider.dimensionId == 0) {
            removeMapStorage(event.world);
            WorldSavedData w = event.world.mapStorage.loadData(StorageManager.class, AE2Thing.MODID);
            if (w == null) {
                AE2ThingAPI.instance()
                    .setStorageManager(new StorageManager(AE2Thing.MODID));
                event.world.mapStorage.setData(
                    AE2Thing.MODID,
                    AE2ThingAPI.instance()
                        .getStorageManager());
            }
        }
    }

    private void removeMapStorage(World world) {
        Map<String, WorldSavedData> m = Ae2Reflect.getLoadedDataMap(world.mapStorage);
        List<WorldSavedData> s = Ae2Reflect.getLoadedDataList(world.mapStorage);
        WorldSavedData w = m.get(AE2Thing.MODID);
        if (w != null) {
            m.remove(AE2Thing.MODID, w);
            s.remove(w);
        }
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
