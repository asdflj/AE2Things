package com.asdflj.ae2thing.crossmod.waila;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.asdflj.ae2thing.util.Util;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.crossmod.thaumcraft.AspectUtil;

import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.common.items.ItemCraftingAspect;

public class TooltipHandlerWaila extends mcp.mobius.waila.handlers.nei.TooltipHandlerWaila {

    @Override
    public List<String> handleItemDisplayName(GuiContainer arg0, ItemStack itemstack, List<String> currenttip) {
        return currenttip;
    }

    @Override
    public List<String> handleItemTooltip(GuiContainer arg0, ItemStack itemstack, int arg2, int arg3,
        List<String> currentToolTip) {
        if (itemstack != null && itemstack.getItem() instanceof ItemFluidDrop && !currentToolTip.isEmpty()) {
            FluidStack fs = ItemFluidDrop.getFluidStack(itemstack);
            if (ModAndClassUtil.THE && AspectUtil.isEssentiaGas(fs)) {
                Aspect aspect = AspectUtil.getAspectFromGas(fs);
                ItemStack is = ItemCraftingAspect.createStackForAspect(aspect, 1);
                currentToolTip.clear();
                currentToolTip.addAll(is.getTooltip(Minecraft.getMinecraft().thePlayer, false));
            } else if (ModAndClassUtil.GT5NH || ModAndClassUtil.GT5) {
                ItemStack potion = Util.getPotion(fs);
                if (potion != null && potion.getItem() instanceof ItemPotion ip) {
                    List<String> potionEffects = new ArrayList<>();
                    ip.addInformation(potion, Minecraft.getMinecraft().thePlayer, potionEffects, false);
                    currentToolTip.addAll(currentToolTip.size() - 1, potionEffects);
                }
            }
        }
        return currentToolTip;
    }

    @Override
    public List<String> handleTooltip(GuiContainer arg0, int arg1, int arg2, List<String> currenttip) {
        return currenttip;
    }
}
