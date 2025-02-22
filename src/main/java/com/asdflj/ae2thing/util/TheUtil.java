package com.asdflj.ae2thing.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.asdflj.ae2thing.common.item.ItemPhial;
import com.asdflj.ae2thing.common.parts.PartThaumatoriumInterface;
import com.asdflj.ae2thing.common.tile.TileInfusionInterface;
import com.glodblock.github.client.gui.GuiDualInterface;
import com.glodblock.github.common.item.ItemFluidDrop;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.helpers.IInterfaceHost;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.api.gui.IWidgetHost;
import thaumicenergistics.common.fluids.GaseousEssentia;
import thaumicenergistics.common.items.ItemCraftingAspect;

public class TheUtil {

    public static boolean isItemCraftingAspect(IAEItemStack item) {
        return (item.getItem() instanceof ItemCraftingAspect);
    }

    public static boolean isGaseousEssentia(IAEFluidStack fluid) {
        if (fluid == null || fluid.getFluid() == null) return false;
        return fluid.getFluid() instanceof GaseousEssentia;
    }

    public static IAEFluidStack itemCraftingAspect2IAEFluidStack(IAEItemStack item) {
        Aspect aspect = ItemCraftingAspect.getAspect(item.getItemStack());
        GaseousEssentia gaseousEssentia = GaseousEssentia.getGasFromAspect(aspect);
        IAEFluidStack fs = AEFluidStack.create(new FluidStack(gaseousEssentia, 1));
        fs.setStackSize(item.getStackSize());
        return fs;
    }

    public static IAEItemStack itemCraftingAspect2FluidDrop(IAEItemStack item) {
        IAEFluidStack fs = itemCraftingAspect2IAEFluidStack(item);
        ItemStack displayStack = ItemFluidDrop.newDisplayStack(fs.getFluidStack());
        IAEItemStack result = AEItemStack.create(displayStack);
        result.setStackSize(fs.getStackSize());
        return result;
    }

    public static String getGuiDualInterfaceDisplayName(String displayName, GuiDualInterface gui) {
        IInterfaceHost host = Ae2ReflectClient.getHost(gui);
        if (host instanceof TileInfusionInterface) {
            return I18n.format(NameConst.GUI_INFUSION_INTERFACE);
        } else if (host instanceof PartThaumatoriumInterface) {
            return I18n.format(NameConst.GUI_PART_THAUMATORIUM_INTERFACE);
        } else {
            return I18n.format(displayName);
        }
    }

    public static IAEItemStack itemCraftingAspect2ItemPhial(IAEItemStack item) {
        Aspect aspect = ItemCraftingAspect.getAspect(item.getItemStack());
        IAEFluidStack fs = ItemPhial.newEssentiaStack(aspect, item.getStackSize());
        return ItemPhial.newAeStack(fs);
    }

    public static IAEItemStack itemPhial2ItemCraftingAspect(IAEItemStack item) {
        Aspect aspect = ItemPhial.getAspect(item.getItemStack());
        IAEItemStack is = AEItemStack.create(ItemCraftingAspect.createStackForAspect(aspect, 1));
        is.setStackSize(item.getStackSize());
        return is;
    }

    @SideOnly(Side.CLIENT)
    public static boolean isTerminal() {
        return Minecraft.getMinecraft().currentScreen instanceof IWidgetHost;
    }

    public static IAEItemStack essentia2CraftingAspect(IAEFluidStack is) {
        if (!isGaseousEssentia(is)) return null;
        GaseousEssentia gas = (GaseousEssentia) is.getFluid();
        return AEItemStack.create(ItemCraftingAspect.createStackForAspect(gas.getAspect(), 1));
    }

    public static boolean isSameAspect(IAEItemStack phial, IAEItemStack craftingAspect) {
        if (ItemPhial.isItemPhial(phial) && TheUtil.isItemCraftingAspect(craftingAspect)) {
            Aspect pa = ItemPhial.getAspect(phial.getItemStack());
            Aspect ca = ItemCraftingAspect.getAspect(craftingAspect.getItemStack());
            if (pa == null) return false;
            return pa.getTag()
                .equals(ca.getTag());
        }
        return false;
    }
}
