package com.asdflj.ae2thing.util;

import net.minecraft.item.ItemStack;

import tconstruct.library.crafting.ModifyBuilder;
import tconstruct.library.modifier.IModifyable;
import tconstruct.tools.TinkerTools;

public class TicUtil {

    private static final ItemStack TOOL_STATION = new ItemStack(TinkerTools.toolStationWood);

    public static boolean isTool(ItemStack is) {
        return is != null && is.getItem() instanceof IModifyable;
    }

    public static ItemStack canModifyItem(ItemStack tool, ItemStack[] items) {
        if (tool != null && items != null && tool.getItem() instanceof IModifyable) {
            return ModifyBuilder.instance.modifyItem(tool, items);
        }
        return null;
    }

    public static ItemStack getToolStation() {
        return TOOL_STATION;
    }
}
