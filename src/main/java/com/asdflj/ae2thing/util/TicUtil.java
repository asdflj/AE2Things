package com.asdflj.ae2thing.util;

import net.minecraft.item.ItemStack;

import tconstruct.library.crafting.ModifyBuilder;
import tconstruct.library.modifier.IModifyable;

public class TicUtil {

    public static boolean isTool(ItemStack is) {
        return is != null && is.getItem() instanceof IModifyable;
    }

    public static ItemStack canModifyItem(ItemStack tool, ItemStack[] items) {
        if (tool != null && items != null && tool.getItem() instanceof IModifyable) {
            return ModifyBuilder.instance.modifyItem(tool, items);
        }
        return null;
    }
}
