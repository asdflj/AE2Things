package com.asdflj.ae2thing.proxy;

import static thaumicenergistics.common.fluids.GaseousEssentia.registerGases;

import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fluids.FluidRegistry;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.api.adapter.findit.EssentiaStorageBusAdapter;
import com.asdflj.ae2thing.api.adapter.findit.FluidStorageBusAdapter;
import com.asdflj.ae2thing.api.adapter.findit.MEChestAdapter;
import com.asdflj.ae2thing.api.adapter.findit.MEDriverAdapter;
import com.asdflj.ae2thing.api.adapter.findit.StorageBusAdapter;
import com.asdflj.ae2thing.api.adapter.terminal.AECraftingTerminal;
import com.asdflj.ae2thing.api.adapter.terminal.FCCraftingTerminal;
import com.asdflj.ae2thing.api.adapter.terminal.WCTCraftingTerminal;
import com.asdflj.ae2thing.common.parts.PartThaumatoriumInterface;
import com.asdflj.ae2thing.common.storage.StorageManager;
import com.asdflj.ae2thing.common.tile.TileInfusionInterface;
import com.asdflj.ae2thing.loader.BRLoader;
import com.asdflj.ae2thing.loader.ItemAndBlockHolder;
import com.asdflj.ae2thing.loader.PatternTerminalLoader;
import com.asdflj.ae2thing.loader.PatternTerminalMouseWheelLoader;
import com.asdflj.ae2thing.network.wrapper.AE2ThingNetworkWrapper;
import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.darkona.adventurebackpack.item.ItemAdventureBackpack;

import appeng.api.config.Upgrades;
import appeng.core.features.registries.InterfaceTerminalRegistry;
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

    public AE2ThingNetworkWrapper netHandler = new AE2ThingNetworkWrapper(AE2Thing.MODID);

    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance()
            .bus()
            .register(this);
        ModAndClassUtil.init();
        if (ModAndClassUtil.BOTANIA) {
            FluidRegistry.registerFluid(
                AE2ThingAPI.instance()
                    .getMana());
        }
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

    public void init(FMLInitializationEvent event) {
        AE2ThingAPI.instance()
            .terminal()
            .registerCraftingTerminal(new AECraftingTerminal());
        AE2ThingAPI.instance()
            .terminal()
            .registerCraftingTerminal(new FCCraftingTerminal());
        if (ModAndClassUtil.WCT) {
            AE2ThingAPI.instance()
                .terminal()
                .registerCraftingTerminal(new WCTCraftingTerminal());
        }
        if (ModAndClassUtil.BLOCK_RENDER) {
            new BRLoader().run();
        }
        new PatternTerminalMouseWheelLoader().run();
        new PatternTerminalLoader().run();

    }

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
        Upgrades.PATTERN_REFILLER.registerItem(ItemAndBlockHolder.ITEM_WIRELESS_DUAL_INTERFACE_TERMINAL.stack(), 1);
        if (ModAndClassUtil.THE) {
            Upgrades.PATTERN_REFILLER.registerItem(ItemAndBlockHolder.INFUSION_PATTERN_TERMINAL.stack(), 1);
            Upgrades.LOCK_CRAFTING.registerItem(ItemAndBlockHolder.INFUSION_INTERFACE.stack(), 1);
            Upgrades.LOCK_CRAFTING.registerItem(ItemAndBlockHolder.THAUMATRIUM_INTERFACE.stack(), 1);
            Upgrades.ADVANCED_BLOCKING.registerItem(ItemAndBlockHolder.INFUSION_INTERFACE.stack(), 1);
            Upgrades.ADVANCED_BLOCKING.registerItem(ItemAndBlockHolder.THAUMATRIUM_INTERFACE.stack(), 1);
            Upgrades.CRAFTING.registerItem(ItemAndBlockHolder.INFUSION_INTERFACE.stack(), 1);
            Upgrades.CRAFTING.registerItem(ItemAndBlockHolder.THAUMATRIUM_INTERFACE.stack(), 1);
            Upgrades.PATTERN_CAPACITY.registerItem(ItemAndBlockHolder.INFUSION_INTERFACE.stack(), 3);
            Upgrades.PATTERN_CAPACITY.registerItem(ItemAndBlockHolder.THAUMATRIUM_INTERFACE.stack(), 3);
            Upgrades.REDSTONE.registerItem(ItemAndBlockHolder.EX_IO_PORT.stack(), 1);
            Upgrades.SPEED.registerItem(ItemAndBlockHolder.EX_IO_PORT.stack(), 3);
            Upgrades.SUPERSPEED.registerItem(ItemAndBlockHolder.EX_IO_PORT.stack(), 3);
            if (ModAndClassUtil.IC2) {
                AE2ThingAPI.instance()
                    .setDefaultFluidContainer(Ic2Items.cell);
            }
            InterfaceTerminalRegistry.instance()
                .register(TileInfusionInterface.class);
            InterfaceTerminalRegistry.instance()
                .register(PartThaumatoriumInterface.class);
        }
        if (ModAndClassUtil.BOTANIA) {
            Upgrades.SPEED.registerItem(ItemAndBlockHolder.MANA_EXPORT_BUS.stack(), 4);
            Upgrades.SUPERSPEED.registerItem(ItemAndBlockHolder.MANA_EXPORT_BUS.stack(), 4);
            Upgrades.REDSTONE.registerItem(ItemAndBlockHolder.MANA_EXPORT_BUS.stack(), 1);
            Upgrades.SPEED.registerItem(ItemAndBlockHolder.MANA_IMPORT_BUS.stack(), 4);
            Upgrades.SUPERSPEED.registerItem(ItemAndBlockHolder.MANA_IMPORT_BUS.stack(), 4);
            Upgrades.REDSTONE.registerItem(ItemAndBlockHolder.MANA_IMPORT_BUS.stack(), 1);
        }
        AE2ThingAPI.instance()
            .terminal()
            .registerFindItStorageProvider(new MEChestAdapter());
        AE2ThingAPI.instance()
            .terminal()
            .registerFindItStorageProvider(new MEDriverAdapter());
        AE2ThingAPI.instance()
            .terminal()
            .registerFindItStorageProvider(new StorageBusAdapter());
        AE2ThingAPI.instance()
            .terminal()
            .registerFindItStorageProvider(new FluidStorageBusAdapter());
        if (ModAndClassUtil.THE) {
            AE2ThingAPI.instance()
                .terminal()
                .registerFindItStorageProvider(new EssentiaStorageBusAdapter());
        }
    }

    public void onLoadComplete(FMLLoadCompleteEvent event) {
        if (ModAndClassUtil.THE && (ModAndClassUtil.GT5NH || ModAndClassUtil.GT5)) {
            // fix terminus essentia not register
            registerGases();
        }
    }

    public void serverStarting(FMLServerStartingEvent event) {

    }

    public void serverStopping(FMLServerStoppingEvent event) {
        StorageManager m = AE2ThingAPI.instance()
            .getStorageManager();
        if (m != null) m.setDirty(true);
    }
}
