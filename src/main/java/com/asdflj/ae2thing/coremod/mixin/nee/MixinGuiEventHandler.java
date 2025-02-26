package com.asdflj.ae2thing.coremod.mixin.nee;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.vfyjxf.nee.client.GuiEventHandler;

@Mixin(GuiEventHandler.class)
public abstract class MixinGuiEventHandler {

    @Inject(method = "handleRecipeIngredientChange", at = @At("HEAD"), remap = false, cancellable = true)
    private static void handleRecipeIngredientChange(GuiContainer gui, Slot currentSlot, int dWheel, CallbackInfo ci) {
        ci.cancel();
    }

}
