package com.asdflj.ae2thing.loader;

import com.asdflj.ae2thing.common.block.BlockFishBig;
import com.asdflj.ae2thing.common.item.ItemBackpackTerminal;
import com.asdflj.ae2thing.common.item.ItemInfinityCell;
import com.asdflj.ae2thing.common.item.ItemInfinityFluidCell;

public class ItemAndBlockHolder {

    public static ItemBackpackTerminal BACKPACK_MANAGER = new ItemBackpackTerminal().register();
    public static ItemInfinityCell ITEM_INFINITY_CELL = new ItemInfinityCell().register();
    public static ItemInfinityFluidCell ITEM_INFINITY_FLUID_CELL = new ItemInfinityFluidCell().register();
    public static BlockFishBig BLOCK_FISH_BIG = new BlockFishBig().register();
}
