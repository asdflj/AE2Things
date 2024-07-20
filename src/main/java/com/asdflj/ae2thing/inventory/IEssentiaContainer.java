package com.asdflj.ae2thing.inventory;

import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.common.item.ItemPhial;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

public interface IEssentiaContainer {

    AspectList getAspects();

    default ItemStack addAspects(ItemStack is) {
        if (is.getItem() instanceof ItemPhial) {
            Aspect aspect = ItemPhial.getAspect(is);
            if (aspect == null) return is;
            int size = is.stackSize;
            long stored = getAspects().getAmount(aspect);
            if (stored + size > Integer.MAX_VALUE) {
                return is;
            } else {
                getAspects().add(aspect, size);
                ItemStack out = is.copy();
                out.stackSize -= size;
                if (out.stackSize <= 0) {
                    return null;
                }
                return out;
            }
        }
        return is;
    }
}
