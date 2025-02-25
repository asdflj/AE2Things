package com.asdflj.ae2thing.coremod.mixin.tc.nei;

import static com.asdflj.ae2thing.coremod.mixin.tc.nei.Util.itemPhial2ItemAspect;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import ru.timeconqueror.tcneiadditions.nei.AspectCombinationHandler;
import ru.timeconqueror.tcneiadditions.nei.TCNACrucibleRecipeHandler;
import ru.timeconqueror.tcneiadditions.nei.TCNAInfusionRecipeHandler;
import ru.timeconqueror.tcneiadditions.nei.arcaneworkbench.ArcaneCraftingShapedHandler;
import ru.timeconqueror.tcneiadditions.nei.arcaneworkbench.ArcaneCraftingShapelessHandler;

@Mixin(
    value = { AspectCombinationHandler.class, ArcaneCraftingShapedHandler.class, ArcaneCraftingShapelessHandler.class,
        TCNACrucibleRecipeHandler.class, TCNAInfusionRecipeHandler.class })
public class MixinTemplateRecipeHandler {

    @ModifyVariable(method = "loadCraftingRecipes*", at = @At("HEAD"), remap = false, ordinal = 0, argsOnly = true)
    public ItemStack ae2thing$loadCraftingRecipes(ItemStack ingredient) {
        return itemPhial2ItemAspect(ingredient);
    }

    @ModifyVariable(method = "loadUsageRecipes", at = @At("HEAD"), remap = false, ordinal = 0, argsOnly = true)
    public ItemStack ae2thing$loadUsageRecipes(ItemStack ingredient) {
        return itemPhial2ItemAspect(ingredient);
    }

}
