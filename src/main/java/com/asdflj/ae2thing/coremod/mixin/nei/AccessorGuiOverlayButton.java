package com.asdflj.ae2thing.coremod.mixin.nei;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import codechicken.nei.recipe.GuiOverlayButton;

@Mixin(GuiOverlayButton.class)
public interface AccessorGuiOverlayButton {

    @Accessor(value = "requireShiftForOverlayRecipe", remap = false)
    void setRequireShiftForOverlayRecipe(boolean value);
}
