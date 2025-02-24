package com.asdflj.ae2thing.coremod.mixin;

import net.minecraft.client.gui.inventory.GuiContainer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.asdflj.ae2thing.client.gui.GuiWirelessDualInterfaceTerminal;
import com.github.vfyjxf.nee.utils.IngredientTracker;

import appeng.client.me.ItemRepo;

@Mixin(value = IngredientTracker.class)
public abstract class MixinIngredientTracker {

    @Final
    @Shadow(remap = false)
    private GuiContainer termGui;

    @Inject(
        method = "getRepo",
        at = @At("HEAD"),
        cancellable = true,
        remap = false,
        locals = LocalCapture.CAPTURE_FAILHARD)
    private void getRepo(CallbackInfoReturnable<ItemRepo> cir) {
        if (termGui instanceof GuiWirelessDualInterfaceTerminal) {
            cir.setReturnValue(((GuiWirelessDualInterfaceTerminal) termGui).getRepo());
            cir.cancel();
        }
    }
}
