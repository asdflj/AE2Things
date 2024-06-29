package com.asdflj.ae2thing.loader;

import static com.asdflj.ae2thing.common.item.ItemCreativeFluidCell.lava_bucket;
import static com.asdflj.ae2thing.common.item.ItemCreativeFluidCell.water_bucket;

import com.asdflj.ae2thing.common.block.BlockFishBig;
import com.asdflj.ae2thing.common.item.ItemBackpackTerminal;
import com.asdflj.ae2thing.common.item.ItemCreativeFluidCell;
import com.asdflj.ae2thing.common.item.ItemInfinityStorageCell;
import com.asdflj.ae2thing.common.item.ItemInfinityStorageFluidCell;
import com.asdflj.ae2thing.common.item.ItemPartDistillationPatternTerminal;
import com.asdflj.ae2thing.util.NameConst;

public class ItemAndBlockHolder {

    public static ItemPartDistillationPatternTerminal DISTILLATION_PATTERN_TERMINAL = new ItemPartDistillationPatternTerminal()
        .register();
    public static ItemBackpackTerminal BACKPACK_MANAGER = new ItemBackpackTerminal().register();
    public static ItemInfinityStorageCell ITEM_INFINITY_CELL = new ItemInfinityStorageCell().register();
    public static ItemInfinityStorageFluidCell ITEM_INFINITY_FLUID_CELL = new ItemInfinityStorageFluidCell().register();
    public static ItemCreativeFluidCell ITEM_CREATIVE_WATER_CELL = new ItemCreativeFluidCell(
        NameConst.ITEM_CREATIVE_FLUID_CELL_WATER,
        water_bucket).register();
    public static ItemCreativeFluidCell ITEM_CREATIVE_LAVA_CELL = new ItemCreativeFluidCell(
        NameConst.ITEM_CREATIVE_FLUID_CELL_LAVA,
        lava_bucket).register();

    public static BlockFishBig BLOCK_FISH_BIG = new BlockFishBig().register();
}
