package com.asdflj.ae2thing;

import net.minecraft.util.ResourceLocation;

import com.asdflj.ae2thing.common.Config;
import com.asdflj.ae2thing.common.storage.CellHandler;
import com.asdflj.ae2thing.crossmod.waila.WailaInit;
import com.asdflj.ae2thing.inventory.InventoryHandler;
import com.asdflj.ae2thing.loader.ChannelLoader;
import com.asdflj.ae2thing.loader.ItemAndBlockHolder;
import com.asdflj.ae2thing.loader.RecipeLoader;
import com.asdflj.ae2thing.proxy.CommonProxy;
import com.asdflj.ae2thing.util.ModAndClassUtil;

import appeng.api.AEApi;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;

@Mod(
    modid = AE2Thing.MODID,
    version = Tags.VERSION,
    name = AE2Thing.NAME,
    dependencies = "required-after:appliedenergistics2;required-after:ae2fc;required-after:ae2stuff;after:thaumicenergistics;after:ic2")
public class AE2Thing {

    public static final String MODID = "ae2thing";
    public static final String NAME = "AE2Thing";

    @Mod.Instance(MODID)
    public static AE2Thing INSTANCE;

    @SidedProxy(
        clientSide = "com.asdflj.ae2thing.proxy.ClientProxy",
        serverSide = "com.asdflj.ae2thing.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        Config.run();
        ChannelLoader.INSTANCE.run();
        proxy.preInit(event);
        ItemAndBlockHolder.INSTANCE.run();
    }

    @Mod.EventHandler
    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
        if (ModAndClassUtil.WAILA) {
            WailaInit.run();
        }
    }

    @Mod.EventHandler
    public void onLoadComplete(FMLLoadCompleteEvent event) {
        proxy.onLoadComplete(event);
    }

    @Mod.EventHandler
    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(AE2Thing.INSTANCE, new InventoryHandler());
        AEApi.instance()
            .registries()
            .cell()
            .addCellHandler(new CellHandler());
        RecipeLoader.INSTANCE.run();
        proxy.postInit(event);
    }

    @Mod.EventHandler
    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }

    @Mod.EventHandler
    // register server commands in this event handler (Remove if not needed)
    public void serverStopping(FMLServerStoppingEvent event) {
        proxy.serverStopping(event);
    }

    public static ResourceLocation resource(String path) {
        return new ResourceLocation(MODID, path);
    }
}
