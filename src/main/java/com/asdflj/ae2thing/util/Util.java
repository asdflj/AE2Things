package com.asdflj.ae2thing.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.common.storage.Constants;

import appeng.api.storage.data.IAEFluidStack;
import appeng.util.Platform;
import appeng.util.item.AEFluidStack;

public class Util {

    private static int randTickSeed = 0;

    public static int findItemStack(EntityPlayer player, ItemStack itemStack) {
        for (int x = 0; x < player.inventory.mainInventory.length; x++) {
            ItemStack item = player.inventory.mainInventory[x];
            if (item == null) continue;
            if (Platform.isSameItemPrecise(item, itemStack)) {
                return x;
            }
        }
        return -1;
    }

    public static long genSingularityFreq() {
        long freq = (new Date()).getTime() * 100 + (randTickSeed) % 100;
        randTickSeed++;
        return freq;
    }

    public static FluidStack getFluidFromItem(ItemStack stack) {
        if (stack != null) {
            if (stack.getItem() instanceof IFluidContainerItem) {
                FluidStack fluid = ((IFluidContainerItem) stack.getItem()).getFluid(stack);
                if (fluid != null) {
                    FluidStack fluid0 = fluid.copy();
                    fluid0.amount *= stack.stackSize;
                    return fluid0;
                }
            }
            if (FluidContainerRegistry.isContainer(stack)) {
                FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(stack);
                if (fluid != null) {
                    FluidStack fluid0 = fluid.copy();
                    fluid0.amount *= stack.stackSize;
                    return fluid0;
                }
            }
        }
        return null;
    }

    public static IAEFluidStack loadFluidStackFromNBT(final NBTTagCompound i) {
        final FluidStack t = FluidRegistry.getFluidStack(i.getString("FluidName"), 1);
        if (t == null) return null;
        final AEFluidStack fluid = AEFluidStack.create(t);
        fluid.setStackSize(i.getLong("Cnt"));
        fluid.setCountRequestable(i.getLong("Req"));
        fluid.setCraftable(i.getBoolean("Craft"));
        return fluid;
    }

    public static List<Integer> getBackpackSlot(EntityPlayer player) {
        List<Integer> result = new ArrayList<>();
        for (int x = 0; x < player.inventory.mainInventory.length; x++) {
            ItemStack item = player.inventory.mainInventory[x];
            if (item == null || item.getItem() == null) continue;
            if (AE2ThingAPI.instance()
                .isBackpackItem(item)) {
                result.add(x);
            }
        }
        return result;
    }

    public static void writeItemStackToNBT(ItemStack itemStack, NBTTagCompound tag) {
        itemStack.writeToNBT(tag);
        tag.setInteger(Constants.COUNT, itemStack.stackSize);
    }

    public static ItemStack loadItemStackFromNBT(NBTTagCompound tag) {
        ItemStack itemStack = ItemStack.loadItemStackFromNBT(tag);
        if (itemStack == null) return null;
        itemStack.stackSize = tag.getInteger(Constants.COUNT);
        return itemStack;
    }
}
