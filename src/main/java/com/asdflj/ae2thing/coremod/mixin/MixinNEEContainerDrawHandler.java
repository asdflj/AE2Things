package com.asdflj.ae2thing.coremod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.vfyjxf.nee.client.NEEContainerDrawHandler;

import codechicken.nei.recipe.GuiRecipe;

@Mixin(NEEContainerDrawHandler.class)
public abstract class MixinNEEContainerDrawHandler {

    @Inject(method = "drawCraftingHelperTooltip", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private void drawCraftingHelperTooltip(GuiRecipe guiRecipe, int mouseX, int mouseY, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "initIngredientTracker", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private void initIngredientTracker(GuiRecipe gui, CallbackInfo ci) {
        ci.cancel();
    }
}
