package com.asdflj.ae2thing.coremod.mixin.tc.nei;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.asdflj.ae2thing.common.item.ItemPhial;
import com.djgiannuzz.thaumcraftneiplugin.ModItems;
import com.djgiannuzz.thaumcraftneiplugin.items.ItemAspect;

import ru.timeconqueror.tcneiadditions.nei.AspectCombinationHandler;
import ru.timeconqueror.tcneiadditions.nei.TCNACrucibleRecipeHandler;
import ru.timeconqueror.tcneiadditions.nei.TCNAInfusionRecipeHandler;
import ru.timeconqueror.tcneiadditions.nei.arcaneworkbench.ArcaneCraftingShapedHandler;
import ru.timeconqueror.tcneiadditions.nei.arcaneworkbench.ArcaneCraftingShapelessHandler;
import thaumcraft.api.aspects.Aspect;

@Mixin(
    value = { AspectCombinationHandler.class, ArcaneCraftingShapedHandler.class, ArcaneCraftingShapelessHandler.class,
        TCNACrucibleRecipeHandler.class, TCNAInfusionRecipeHandler.class })
public class MixinTemplateRecipeHandler {

    @ModifyVariable(method = "loadCraftingRecipes*", at = @At("HEAD"), remap = false, ordinal = 0, argsOnly = true)
    public ItemStack ae2thing$loadCraftingRecipes(ItemStack ingredient) {
        if (ingredient.getItem() instanceof ItemPhial) {
            ItemStack result = new ItemStack(ModItems.itemAspect);
            Aspect aspect = ItemPhial.getAspect(ingredient);
            ItemAspect.setAspect(result, aspect);
            return result;
        }
        return ingredient;
    }

    @ModifyVariable(method = "loadUsageRecipes", at = @At("HEAD"), remap = false, ordinal = 0, argsOnly = true)
    public ItemStack ae2thing$loadUsageRecipes(ItemStack ingredient) {
        if (ingredient.getItem() instanceof ItemPhial) {
            ItemStack result = new ItemStack(ModItems.itemAspect);
            Aspect aspect = ItemPhial.getAspect(ingredient);
            ItemAspect.setAspect(result, aspect);
            return result;
        }
        return ingredient;
    }
}
