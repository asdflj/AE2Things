package com.asdflj.ae2thing.loader;

import com.asdflj.ae2thing.common.item.ItemBackpackManager;
import com.asdflj.ae2thing.common.item.ItemInfinityCell;

public class ItemAndBlockHolder {

    public static ItemBackpackManager BACKPACK_MANAGER = new ItemBackpackManager().register();
    public static ItemInfinityCell ITEM_INFINITY_CELL = new ItemInfinityCell().register();
}
