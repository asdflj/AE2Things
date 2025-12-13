package com.asdflj.ae2thing.coremod.mixin.nei;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.common.MinecraftForge;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.asdflj.ae2thing.client.event.GuiOverlayButtonEvent;

import codechicken.nei.recipe.GuiOverlayButton;
import codechicken.nei.recipe.GuiRecipeButton;
import codechicken.nei.recipe.RecipeHandlerRef;

@Mixin(GuiOverlayButton.class)
public abstract class MixinGuiOverlayButton extends GuiRecipeButton {

    @Final
    public GuiContainer firstGui;

    protected MixinGuiOverlayButton(RecipeHandlerRef point, int x, int y, int buttonId, String label) {
        super(point, x, y, buttonId, label);
    }

    @Inject(method = "mouseReleased", at = @At(value = "TAIL"))
    public void mouseReleased(int mouseX, int mouseY, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new GuiOverlayButtonEvent((GuiOverlayButton) ((Object) this)));
    }
}
