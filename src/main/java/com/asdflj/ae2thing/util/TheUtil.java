package com.asdflj.ae2thing.util;

import net.minecraftforge.fluids.FluidStack;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEFluidStack;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.common.fluids.GaseousEssentia;
import thaumicenergistics.common.items.ItemCraftingAspect;

public class TheUtil {

    public static boolean isItemCraftingAspect(IAEItemStack item) {
        return (item.getItem() instanceof ItemCraftingAspect);
    }

    public static IAEFluidStack ItemCraftingAspect2IAEFluidStack(IAEItemStack item) {
        Aspect aspect = ItemCraftingAspect.getAspect(item.getItemStack());
        GaseousEssentia gaseousEssentia = GaseousEssentia.getGasFromAspect(aspect);
        return AEFluidStack.create(new FluidStack(gaseousEssentia, 1));
    }
}
