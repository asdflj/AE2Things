package com.asdflj.ae2thing.loader;

import static com.asdflj.ae2thing.loader.ItemAndBlockHolder.BACKPACK_MANAGER;
import static com.asdflj.ae2thing.loader.ItemAndBlockHolder.BLOCK_FISHBIG;
import static com.asdflj.ae2thing.loader.ItemAndBlockHolder.BLOCK_MDDyue;
import static com.asdflj.ae2thing.loader.ItemAndBlockHolder.CRAFTING_DEBUG_CARD;
import static com.asdflj.ae2thing.loader.ItemAndBlockHolder.ESSENTIA_DISCRETIZER;
import static com.asdflj.ae2thing.loader.ItemAndBlockHolder.EX_IO_PORT;
import static com.asdflj.ae2thing.loader.ItemAndBlockHolder.FLUID_PACKET_ENCODER;
import static com.asdflj.ae2thing.loader.ItemAndBlockHolder.INFUSION_INTERFACE;
import static com.asdflj.ae2thing.loader.ItemAndBlockHolder.INFUSION_PATTERN_TERMINAL;
import static com.asdflj.ae2thing.loader.ItemAndBlockHolder.ITEM_CREATIVE_COBBLESTONE_CELL;
import static com.asdflj.ae2thing.loader.ItemAndBlockHolder.ITEM_CREATIVE_WATER_CELL;
import static com.asdflj.ae2thing.loader.ItemAndBlockHolder.ITEM_INFINITY_CELL;
import static com.asdflj.ae2thing.loader.ItemAndBlockHolder.ITEM_INFINITY_FLUID_CELL;
import static com.asdflj.ae2thing.loader.ItemAndBlockHolder.ITEM_PATTERN_MODIFIER;
import static com.asdflj.ae2thing.loader.ItemAndBlockHolder.ITEM_WIRELESS_CONNECTOR_TERMINAL;
import static com.asdflj.ae2thing.loader.ItemAndBlockHolder.ITEM_WIRELESS_DUAL_INTERFACE_TERMINAL;
import static com.asdflj.ae2thing.loader.ItemAndBlockHolder.MANA_EXPORT_BUS;
import static com.asdflj.ae2thing.loader.ItemAndBlockHolder.MANA_IMPORT_BUS;
import static com.asdflj.ae2thing.loader.ItemAndBlockHolder.THAUMATRIUM_INTERFACE;
import static com.asdflj.ae2thing.loader.ItemAndBlockHolder.WIRELESS_CONNECTOR_TERMINAL;
import static com.glodblock.github.loader.ItemAndBlockHolder.CELL_HOUSING;
import static com.glodblock.github.loader.ItemAndBlockHolder.FLUID_TERMINAL_EX;
import static com.glodblock.github.loader.ItemAndBlockHolder.WIRELESS_INTERFACE_TERM;
import static thaumcraft.common.config.ConfigItems.itemResource;
import static thaumcraft.common.config.ConfigItems.itemThaumonomicon;
import static thaumicenergistics.common.blocks.BlockEnum.DISTILLATION_ENCODER;

import net.bdew.ae2stuff.machines.wireless.MachineWireless;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.common.Config;
import com.asdflj.ae2thing.loader.recipe.WirelessTerminalEnergyRecipe;
import com.asdflj.ae2thing.loader.recipe.WirelessTerminalQuantumBridgeRecipe;
import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.asdflj.ae2thing.util.TicUtil;
import com.glodblock.github.common.storage.CellType;

import appeng.api.AEApi;
import cpw.mods.fml.common.registry.GameRegistry;
import thaumcraft.common.config.ConfigBlocks;

public class RecipeLoader implements Runnable {

    public static final RecipeLoader INSTANCE = new RecipeLoader();
    public static final ItemStack CHEST = new ItemStack(Blocks.chest, 1);
    public static final ItemStack CRAFTING_TABLE = new ItemStack(Blocks.crafting_table, 1);
    public static final ItemStack DIAMOND = new ItemStack(Items.diamond, 1);
    public static final ItemStack FISH = new ItemStack(Items.fish);
    public static final ItemStack EGG = new ItemStack(Items.egg);
    public static final ItemStack AE2_PATTERN_TERM = new ItemStack(
        GameRegistry.findItem("appliedenergistics2", "item.ItemMultiPart"),
        1,
        340);
    public static final ItemStack AE2_DIGITAL_SINGULARITY_CELL = new ItemStack(
        GameRegistry.findItem("appliedenergistics2", "item.ItemExtremeStorageCell.Singularity"),
        1);
    public static final ItemStack AE2FC_DIGITAL_SINGULARITY_CELL = com.glodblock.github.loader.ItemAndBlockHolder.SINGULARITY_CELL
        .stack();
    public static final ItemStack AE2_WIRELESS_TERMINAL = GameRegistry
        .findItemStack("appliedenergistics2", "item.ToolWirelessTerminal", 1);
    public static final ItemStack AE2_TERMINAL = new ItemStack(
        GameRegistry.findItem("appliedenergistics2", "item.ItemMultiPart"),
        1,
        380);
    public static final ItemStack AE2_ME_IO_PORT = AEApi.instance()
        .definitions()
        .blocks()
        .iOPort()
        .maybeStack(1)
        .get();
    public static final ItemStack AE2_SINGULARITY = AEApi.instance()
        .definitions()
        .materials()
        .singularity()
        .maybeStack(1)
        .get();
    public static final ItemStack AE2_SUPER_SPEED_CARD = new ItemStack(
        GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial"),
        1,
        56);
    public static final ItemStack AE2_MEMORY_CARD = AEApi.instance()
        .definitions()
        .items()
        .memoryCard()
        .maybeStack(1)
        .get();

    public static final ItemStack AE2_CRAFTING_CARD = AEApi.instance()
        .definitions()
        .materials()
        .cardCrafting()
        .maybeStack(1)
        .get();

    public static final ItemStack AE2_ADV_HOUSING = AEApi.instance()
        .definitions()
        .materials()
        .emptyAdvancedStorageCell()
        .maybeStack(1)
        .get();
    public static final ItemStack AE2_64K_PART = AEApi.instance()
        .definitions()
        .materials()
        .cell64kPart()
        .maybeStack(1)
        .get();
    public static final ItemStack AE2_BLANK_PATTERN = AEApi.instance()
        .definitions()
        .materials()
        .blankPattern()
        .maybeStack(1)
        .get();
    public static final ItemStack AE2_PROCESS_LOG = AEApi.instance()
        .definitions()
        .materials()
        .logicProcessor()
        .maybeStack(1)
        .get();
    public static final ItemStack AE2_VIEW_CELL = AEApi.instance()
        .definitions()
        .items()
        .viewCell()
        .maybeStack(1)
        .get();

    @Override
    public void run() {
        // GameRegistry.addShapelessRecipe(TOGGLE_VIEW_CELL.stack(), AE2_VIEW_CELL, AE2_PROCESS_LOG);
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ITEM_PATTERN_MODIFIER.stack(),
                "   ",
                "GPG",
                " L ",
                'G',
                "dyeGreen",
                'P',
                AE2_BLANK_PATTERN,
                'L',
                AE2_PROCESS_LOG));
        GameRegistry.addShapelessRecipe(ITEM_CREATIVE_COBBLESTONE_CELL.stack(), AE2_ADV_HOUSING, AE2_64K_PART);
        GameRegistry.addShapelessRecipe(CRAFTING_DEBUG_CARD.stack(), AE2_MEMORY_CARD, AE2_CRAFTING_CARD);
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                BACKPACK_MANAGER.stack(),
                "TDT",
                "TCT",
                "TDT",
                'C',
                CHEST,
                'D',
                DIAMOND,
                'T',
                ModAndClassUtil.TIC && Config.backpackTerminalAddTicSupport ? TicUtil.getToolStation()
                    : CRAFTING_TABLE));
        GameRegistry.addRecipe(
            new ShapedOreRecipe(ITEM_INFINITY_CELL.stack(), "CCC", "CCC", "CCC", 'C', AE2_DIGITAL_SINGULARITY_CELL));
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ITEM_INFINITY_FLUID_CELL.stack(),
                "CCC",
                "CCC",
                "CCC",
                'C',
                AE2FC_DIGITAL_SINGULARITY_CELL));
        GameRegistry.addShapelessRecipe(
            ITEM_WIRELESS_CONNECTOR_TERMINAL.stack(),
            AE2_WIRELESS_TERMINAL,
            MachineWireless.block());
        GameRegistry.addShapelessRecipe(WIRELESS_CONNECTOR_TERMINAL.stack(), AE2_TERMINAL, MachineWireless.block());
        GameRegistry.addShapelessRecipe(
            EX_IO_PORT.stack(),
            AE2_ME_IO_PORT,
            AE2_SINGULARITY,
            AE2_SUPER_SPEED_CARD,
            AE2_SUPER_SPEED_CARD);
        GameRegistry.addShapelessRecipe(
            ITEM_WIRELESS_DUAL_INTERFACE_TERMINAL.stack(),
            WIRELESS_INTERFACE_TERM,
            FLUID_TERMINAL_EX.stack());
        GameRegistry.addShapelessRecipe(
            FLUID_PACKET_ENCODER.stack(),
            com.glodblock.github.loader.ItemAndBlockHolder.DECODER.stack());
        GameRegistry.addShapelessRecipe(
            com.glodblock.github.loader.ItemAndBlockHolder.DECODER.stack(),
            FLUID_PACKET_ENCODER.stack());
        if (ModAndClassUtil.THE) {
            final ItemStack THAUMIUM_INGOT = new ItemStack(itemResource, 1, 2);
            final ItemStack RUNIC_MATRIX = new ItemStack(ConfigBlocks.blockStoneDevice, 1, 2);
            final ItemStack THAUMONOMICON = new ItemStack(itemThaumonomicon, 1);
            final ItemStack THE_DISTILLATION_ENCODER = new ItemStack(DISTILLATION_ENCODER.getBlock(), 1);
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    INFUSION_PATTERN_TERMINAL.stack(),
                    "IRI",
                    "DPE",
                    "ICI",
                    'P',
                    AE2_PATTERN_TERM,
                    'I',
                    THAUMIUM_INGOT,
                    'R',
                    RUNIC_MATRIX,
                    'C',
                    THAUMONOMICON,
                    'D',
                    THE_DISTILLATION_ENCODER,
                    'E',
                    ESSENTIA_DISCRETIZER.stack()));
            GameRegistry.addShapelessRecipe(
                INFUSION_INTERFACE.stack(),
                com.glodblock.github.loader.ItemAndBlockHolder.INTERFACE,
                AE2ThingAPI.PHIAL);
            GameRegistry.addShapelessRecipe(
                ESSENTIA_DISCRETIZER.stack(),
                com.glodblock.github.loader.ItemAndBlockHolder.DISCRETIZER,
                AE2ThingAPI.PHIAL);
            GameRegistry.addShapelessRecipe(INFUSION_INTERFACE.stack(), THAUMATRIUM_INTERFACE.stack());
            GameRegistry.addShapelessRecipe(THAUMATRIUM_INTERFACE.stack(), INFUSION_INTERFACE.stack());

        }
        if (ModAndClassUtil.BOTANIA) {
            GameRegistry.addShapelessRecipe(
                MANA_IMPORT_BUS.stack(),
                com.glodblock.github.loader.ItemAndBlockHolder.FLUID_IMPORT_BUS.stack());
            GameRegistry.addShapelessRecipe(
                MANA_EXPORT_BUS.stack(),
                com.glodblock.github.loader.ItemAndBlockHolder.FLUID_EXPORT_BUS.stack());
            GameRegistry.addShapelessRecipe(
                com.glodblock.github.loader.ItemAndBlockHolder.FLUID_IMPORT_BUS.stack(),
                MANA_IMPORT_BUS.stack());
            GameRegistry.addShapelessRecipe(
                com.glodblock.github.loader.ItemAndBlockHolder.FLUID_EXPORT_BUS.stack(),
                MANA_EXPORT_BUS.stack());
        }
        WirelessTerminalQuantumBridgeRecipe.register(ITEM_WIRELESS_DUAL_INTERFACE_TERMINAL.stack());
        WirelessTerminalEnergyRecipe.register(ITEM_WIRELESS_DUAL_INTERFACE_TERMINAL.stack());
        GameRegistry.addRecipe(new ShapedOreRecipe(BLOCK_FISHBIG.stack(), "FFF", "F F", "FFF", 'F', FISH));
        GameRegistry.addRecipe(new ShapedOreRecipe(BLOCK_MDDyue.stack(), "EEE", "E E", "EEE", 'E', EGG));
        GameRegistry.addRecipe(
            new ShapelessOreRecipe(ITEM_CREATIVE_WATER_CELL, CELL_HOUSING.stack(1, 1), CellType.Cell64kPart.stack(1)));
    }
}
