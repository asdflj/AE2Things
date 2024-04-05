package com.asdflj.ae2thing.common.tabs;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.loader.ItemAndBlockHolder;

public class AE2ThingTabs extends CreativeTabs {

    public static final AE2ThingTabs INSTANCE = new AE2ThingTabs(AE2Thing.MODID);

    public AE2ThingTabs(String name) {
        super(name);
    }

    @Override
    public Item getTabIconItem() {
        return ItemAndBlockHolder.BACKPACK_MANAGER;
    }

}
