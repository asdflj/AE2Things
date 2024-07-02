package com.asdflj.ae2thing.util;

import static net.minecraft.init.Items.glass_bottle;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

import org.apache.commons.lang3.tuple.MutablePair;

import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.common.item.ItemBackpackTerminal;

import appeng.api.storage.data.IAEFluidStack;
import appeng.util.Platform;
import appeng.util.item.AEFluidStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class Util {

    private static int randTickSeed = 0;

    public static int findBackPackTerminal(EntityPlayer player) {
        for (int x = 0; x < player.inventory.mainInventory.length; x++) {
            ItemStack item = player.inventory.mainInventory[x];
            if (item == null || item.getItem() == null) continue;
            if (item.getItem() instanceof ItemBackpackTerminal) return x;
        }
        return -1;
    }

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

    @SideOnly(Side.CLIENT)
    public static int getLimitFPS() {
        Minecraft mc = Minecraft.getMinecraft();
        return mc.gameSettings.limitFramerate;
    }

    @SideOnly(Side.CLIENT)
    public static int getCurrentFPS() {
        try {
            Field field = Minecraft.class.getDeclaredField("debugFPS");
            field.setAccessible(true);
            return field.getInt(Minecraft.getMinecraft());
        } catch (Exception ignored) {}
        return 0;
    }

    public static final ItemStack GLASS_BOTTLE = new ItemStack(glass_bottle, 1);

    public static ItemStack getPotion(FluidStack fs) {
        if (fs == null) return null;
        MutablePair<Integer, ItemStack> fillStack = com.glodblock.github.util.Util.FluidUtil
            .fillStack(GLASS_BOTTLE, fs);
        if (fillStack != null && fillStack.getRight() != null
            && fillStack.getRight()
                .getItem() instanceof ItemPotion) {
            return fillStack.right;
        }
        return null;
    }
}
