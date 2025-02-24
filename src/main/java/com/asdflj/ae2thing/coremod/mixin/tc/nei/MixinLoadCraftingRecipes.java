package com.asdflj.ae2thing.coremod.mixin.tc.nei;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.asdflj.ae2thing.common.item.ItemPhial;
import com.djgiannuzz.thaumcraftneiplugin.ModItems;
import com.djgiannuzz.thaumcraftneiplugin.items.ItemAspect;

import codechicken.nei.recipe.TemplateRecipeHandler;
import ru.timeconqueror.tcneiadditions.nei.AspectFromItemStackHandler;
import thaumcraft.api.aspects.Aspect;

@Mixin(AspectFromItemStackHandler.class)
public abstract class MixinLoadCraftingRecipes extends TemplateRecipeHandler {

    @ModifyVariable(method = "loadCraftingRecipes", at = @At("HEAD"), remap = false, ordinal = 0, argsOnly = true)
    public ItemStack ae2thing$loadCraftingRecipes(ItemStack ingredient) {
        if (ingredient.getItem() instanceof ItemPhial) {
            ItemStack result = new ItemStack(ModItems.itemAspect);
            Aspect aspect = ItemPhial.getAspect(ingredient);
            ItemAspect.setAspect(result, aspect);
            return result;
        }
        return ingredient;
    }

}
