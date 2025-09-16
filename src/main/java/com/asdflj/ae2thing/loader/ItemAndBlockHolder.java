package com.asdflj.ae2thing.loader;

import static com.asdflj.ae2thing.common.item.ItemCreativeCell.cobblestone;
import static com.asdflj.ae2thing.common.item.ItemCreativeFluidCell.lava_bucket;
import static com.asdflj.ae2thing.common.item.ItemCreativeFluidCell.water_bucket;

import com.asdflj.ae2thing.common.block.BaseDollBlockContainer;
import com.asdflj.ae2thing.common.block.BlockEssentiaDiscretizer;
import com.asdflj.ae2thing.common.block.BlockExIOPort;
import com.asdflj.ae2thing.common.block.BlockFluidPacketEncoder;
import com.asdflj.ae2thing.common.block.BlockInfusionInterface;
import com.asdflj.ae2thing.common.item.ItemBackpackTerminal;
import com.asdflj.ae2thing.common.item.ItemCraftingDebugCard;
import com.asdflj.ae2thing.common.item.ItemCreativeCell;
import com.asdflj.ae2thing.common.item.ItemCreativeFluidCell;
import com.asdflj.ae2thing.common.item.ItemInfinityStorageCell;
import com.asdflj.ae2thing.common.item.ItemInfinityStorageFluidCell;
import com.asdflj.ae2thing.common.item.ItemManaExportBus;
import com.asdflj.ae2thing.common.item.ItemManaImportBus;
import com.asdflj.ae2thing.common.item.ItemPartInfusionPatternTerminal;
import com.asdflj.ae2thing.common.item.ItemPartThaumatoriumInterface;
import com.asdflj.ae2thing.common.item.ItemPartWirelessConnectorTerminal;
import com.asdflj.ae2thing.common.item.ItemPatternModifier;
import com.asdflj.ae2thing.common.item.ItemPhial;
import com.asdflj.ae2thing.common.item.ItemWirelessConnectorTerminal;
import com.asdflj.ae2thing.common.item.ItemWirelessDualInterfaceTerminal;
import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.asdflj.ae2thing.util.NameConst;

public class ItemAndBlockHolder implements Runnable {

    public static final ItemAndBlockHolder INSTANCE = new ItemAndBlockHolder();

    public static ItemBackpackTerminal BACKPACK_MANAGER = new ItemBackpackTerminal().register();
    public static ItemPartInfusionPatternTerminal INFUSION_PATTERN_TERMINAL;
    public static ItemPartThaumatoriumInterface THAUMATRIUM_INTERFACE;
    public static ItemCraftingDebugCard CRAFTING_DEBUG_CARD = new ItemCraftingDebugCard().register();
    // public static ItemToggleableViewCell TOGGLE_VIEW_CELL = new ItemToggleableViewCell().register();
    public static BlockInfusionInterface INFUSION_INTERFACE;
    public static ItemPhial PHIAL;
    public static ItemManaImportBus MANA_IMPORT_BUS;
    public static ItemManaExportBus MANA_EXPORT_BUS;
    public static BlockEssentiaDiscretizer ESSENTIA_DISCRETIZER;
    public static BlockFluidPacketEncoder FLUID_PACKET_ENCODER = new BlockFluidPacketEncoder().register();
    public static ItemInfinityStorageCell ITEM_INFINITY_CELL = new ItemInfinityStorageCell().register();
    public static ItemInfinityStorageFluidCell ITEM_INFINITY_FLUID_CELL = new ItemInfinityStorageFluidCell().register();
    public static ItemPartWirelessConnectorTerminal WIRELESS_CONNECTOR_TERMINAL = new ItemPartWirelessConnectorTerminal()
        .register();
    public static ItemWirelessConnectorTerminal ITEM_WIRELESS_CONNECTOR_TERMINAL = new ItemWirelessConnectorTerminal()
        .register();
    public static ItemWirelessDualInterfaceTerminal ITEM_WIRELESS_DUAL_INTERFACE_TERMINAL = new ItemWirelessDualInterfaceTerminal()
        .register();
    public static ItemPatternModifier ITEM_PATTERN_MODIFIER = new ItemPatternModifier().register();
    public static ItemCreativeFluidCell ITEM_CREATIVE_WATER_CELL = new ItemCreativeFluidCell(
        NameConst.ITEM_CREATIVE_FLUID_CELL_WATER,
        water_bucket).register();
    public static ItemCreativeFluidCell ITEM_CREATIVE_LAVA_CELL = new ItemCreativeFluidCell(
        NameConst.ITEM_CREATIVE_FLUID_CELL_LAVA,
        lava_bucket).register();
    public static ItemCreativeCell ITEM_CREATIVE_COBBLESTONE_CELL = new ItemCreativeCell(
        NameConst.ITEM_CREATIVE_CELL_COBBLESTONE,
        cobblestone,
        String.format("%s_%s", NameConst.ITEM_CREATIVE_CELL, "cobblestone")).register();
    public static BlockExIOPort EX_IO_PORT = new BlockExIOPort().register();

    public static BaseDollBlockContainer BLOCK_FISHBIG = new BaseDollBlockContainer(NameConst.BLOCK_FISHBIG).register();

    public static BaseDollBlockContainer BLOCK_MDDyue = new BaseDollBlockContainer(NameConst.BLOCK_MDDyue).register();

    @Override
    public void run() {
        if (ModAndClassUtil.THE) {
            INFUSION_PATTERN_TERMINAL = new ItemPartInfusionPatternTerminal().register();
            THAUMATRIUM_INTERFACE = new ItemPartThaumatoriumInterface().register();
            INFUSION_INTERFACE = new BlockInfusionInterface().register();
            PHIAL = new ItemPhial().register();
            ESSENTIA_DISCRETIZER = new BlockEssentiaDiscretizer().register();
        }
        if (ModAndClassUtil.BOTANIA) {
            MANA_IMPORT_BUS = new ItemManaImportBus().register();
            MANA_EXPORT_BUS = new ItemManaExportBus().register();
        }
    }
}
