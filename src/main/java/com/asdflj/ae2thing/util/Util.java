package com.asdflj.ae2thing.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

import org.apache.commons.lang3.tuple.MutablePair;

import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.common.item.ItemBackpackTerminal;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.crossmod.thaumcraft.AspectUtil;

import appeng.api.AEApi;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.DimensionalCoord;
import appeng.items.tools.powered.ToolWirelessTerminal;
import appeng.util.Platform;
import appeng.util.item.AEFluidStack;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thaumicenergistics.common.integration.tc.AspectHooks;

public class Util {

    public static boolean isSameDimensionalCoord(DimensionalCoord a, DimensionalCoord b) {
        return a.x == b.x && a.y == b.y && a.z == b.z && a.getDimension() == b.getDimension();
    }

    private static int randTickSeed = 0;

    public static int findBackPackTerminal(EntityPlayer player) {
        for (int x = 0; x < player.inventory.mainInventory.length; x++) {
            ItemStack item = player.inventory.mainInventory[x];
            if (item == null || item.getItem() == null) continue;
            if (item.getItem() instanceof ItemBackpackTerminal) return x;
        }
        return -1;
    }

    public static IGrid getWirelessGrid(EntityPlayer player) {
        for (int x = 0; x < player.inventory.mainInventory.length; x++) {
            ItemStack item = player.inventory.mainInventory[x];
            if (item == null || item.getItem() == null) continue;
            IGridNode node = getWirelessGridNode(item);
            if (node == null) continue;
            return node.getGrid();
        }
        return null;
    }

    public static String getModId(IAEItemStack item) {
        if (item.getItem() instanceof ItemFluidDrop) {
            FluidStack fs = ItemFluidDrop.getFluidStack(item.getItemStack());
            if (fs == null) return GameRegistry.findUniqueIdentifierFor(item.getItem()).modId;
            if (ModAndClassUtil.THE && AspectUtil.isEssentiaGas(fs)) {
                ModContainer mod = AspectHooks.aspectToMod.getOrDefault(AspectUtil.getAspectFromGas(fs), null);
                if (mod != null) return mod.getModId();
            } else {
                return getFluidModID(fs.getFluid());
            }
        }
        return Platform.getModId(item);
    }

    public static String getFluidModID(Fluid fluid) {
        String name = FluidRegistry.getDefaultFluidName(fluid);
        try {
            return name.split(":")[0];
        } catch (Exception e) {
            return "";
        }
    }

    @Nonnull
    public static String getDisplayName(IAEItemStack item) {
        if (item.getItem() instanceof ItemFluidDrop) {
            FluidStack fs = ItemFluidDrop.getFluidStack(item.getItemStack());
            if (fs == null) return Platform.getItemDisplayName(item);
            if (ModAndClassUtil.THE && AspectUtil.isEssentiaGas(fs)) {
                return AspectUtil.getAspectFromGas(fs)
                    .getName();
            } else {
                return fs.getLocalizedName();
            }
        }
        return Platform.getItemDisplayName(item);
    }

    public static IGridHost getWirelessGridHost(ItemStack is) {
        if (is.getItem() instanceof ToolWirelessTerminal) {
            String key = ((ToolWirelessTerminal) is.getItem()).getEncryptionKey(is);
            return (IGridHost) AEApi.instance()
                .registries()
                .locatable()
                .getLocatableBy(Long.parseLong(key));
        }
        return null;
    }

    public static IGridNode getWirelessGridNode(ItemStack is) {
        IGridHost host = getWirelessGridHost(is);
        if (host == null) return null;
        return host.getGridNode(ForgeDirection.UNKNOWN);
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

    public static ItemStack getPotion(FluidStack fs) {
        if (fs == null) return null;
        MutablePair<Integer, ItemStack> fillStack = com.glodblock.github.util.Util.FluidUtil
            .fillStack(AE2ThingAPI.GLASS_BOTTLE, fs);
        if (fillStack != null && fillStack.getRight() != null
            && fillStack.getRight()
                .getItem() instanceof ItemPotion) {
            return fillStack.right;
        }
        return null;
    }
}
