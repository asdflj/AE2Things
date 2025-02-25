package com.asdflj.ae2thing.coremod.mixin.nee;

import net.minecraft.client.gui.GuiScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.vfyjxf.nee.utils.GuiUtils;

@Mixin(GuiUtils.class)
public abstract class MixinGuiUtils {

    @Inject(
        method = { "isGuiCraftingTerm", "isGuiWirelessCrafting" },
        at = @At("HEAD"),
        cancellable = true,
        remap = false)
    private static void isGuiCraftingTerm(GuiScreen guiScreen, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
