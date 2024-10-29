package com.asdflj.ae2thing.util;

import net.minecraft.item.ItemStack;

import com.glodblock.github.hbmaeaddon.util.HBMFluidBridge;
import com.glodblock.github.hbmaeaddon.util.HBMUtil;
import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;

import appeng.api.storage.data.IAEFluidStack;

public class HBMAeAddonUtil {

    public static boolean getItemHasFluidType(ItemStack is) {
        return HBMUtil.getFluidType(is) != Fluids.NONE;
    }

    public static boolean getItemIsEmptyContainer(ItemStack is, IAEFluidStack fs) {
        FluidType f = HBMFluidBridge.get(fs.getFluid());
        if (f == Fluids.NONE) return false;
        return FluidContainerRegistry.allContainers.stream()
            .anyMatch(
                i -> i.emptyContainer != null && i.emptyContainer.getItem() == is.getItem()
                    && i.type == HBMFluidBridge.get(fs.getFluid()));
    }

    public static ItemStack getFillContainer(ItemStack is, IAEFluidStack fs) {
        FluidType f = HBMFluidBridge.get(fs.getFluid());
        return FluidContainerRegistry.getFullContainer(is, f);
    }

    public static int getEmptyContainerAmount(ItemStack is, IAEFluidStack fs) {
        ItemStack filled = getFillContainer(is, fs);
        FluidType f = HBMFluidBridge.get(fs.getFluid());
        return FluidContainerRegistry.getFluidContent(filled, f);
    }

    public static net.minecraftforge.fluids.FluidStack getFluidPerContainer(ItemStack is) {
        FluidType type = HBMUtil.getFluidType(is);
        int fluidPerContainer = FluidContainerRegistry.getFluidContent(is, type);
        return new net.minecraftforge.fluids.FluidStack(HBMFluidBridge.get(type), fluidPerContainer);
    }
}
