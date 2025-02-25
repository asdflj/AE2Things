package com.asdflj.ae2thing.coremod.mixin.tc.nei;

import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.common.item.ItemPhial;
import com.djgiannuzz.thaumcraftneiplugin.ModItems;
import com.djgiannuzz.thaumcraftneiplugin.items.ItemAspect;

import thaumcraft.api.aspects.Aspect;

public class Util {

    public static ItemStack itemPhial2ItemAspect(ItemStack item) {
        if (item.getItem() instanceof ItemPhial) {
            ItemStack result = new ItemStack(ModItems.itemAspect);
            Aspect aspect = ItemPhial.getAspect(item);
            ItemAspect.setAspect(result, aspect);
            return result;
        }
        return item;
    }
}
