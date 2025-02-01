package com.asdflj.ae2thing.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fluids.FluidStack;

import com.asdflj.ae2thing.common.parts.PartThaumatoriumInterface;
import com.asdflj.ae2thing.common.tile.TileInfusionInterface;
import com.glodblock.github.client.gui.GuiDualInterface;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.helpers.IInterfaceHost;
import appeng.util.item.AEFluidStack;
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

    public static IAEFluidStack ItemCraftingAspect2IAEFluidStack(IAEItemStack item) {
        Aspect aspect = ItemCraftingAspect.getAspect(item.getItemStack());
        GaseousEssentia gaseousEssentia = GaseousEssentia.getGasFromAspect(aspect);
        return AEFluidStack.create(new FluidStack(gaseousEssentia, 1));
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

    @SideOnly(Side.CLIENT)
    public static boolean isTerminal() {
        return Minecraft.getMinecraft().currentScreen instanceof IWidgetHost;
    }
}
