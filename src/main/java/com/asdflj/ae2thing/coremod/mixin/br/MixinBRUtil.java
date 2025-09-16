package com.asdflj.ae2thing.coremod.mixin.br;

import static com.asdflj.ae2thing.util.BRUtil.getIngredients;
import static com.asdflj.ae2thing.util.BRUtil.sendToServer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import blockrenderer6343.client.renderer.WorldSceneRenderer;
import blockrenderer6343.client.utils.BRUtil;

@Mixin(BRUtil.class)
public class MixinBRUtil {

    @Inject(method = "neiOverlay", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private static void ae2thing$neiOverlay(WorldSceneRenderer renderer, CallbackInfo ci) {
        if (sendToServer(getIngredients(renderer))) {
            ci.cancel();
        }
    }
}
