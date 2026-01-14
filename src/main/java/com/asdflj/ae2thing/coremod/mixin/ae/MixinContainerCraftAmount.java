package com.asdflj.ae2thing.coremod.mixin.ae;

import net.minecraft.entity.player.InventoryPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.container.AEBaseContainer;
import appeng.container.implementations.ContainerCraftAmount;

@Mixin(ContainerCraftAmount.class)
public abstract class MixinContainerCraftAmount extends AEBaseContainer {

    public MixinContainerCraftAmount(InventoryPlayer ip, Object anchor) {
        super(ip, anchor);
    }

    @Inject(method = "onUpdate", at = @At("HEAD"), cancellable = true, remap = false)
    public void onUpdate(String field, Object oldValue, Object newValue, CallbackInfo ci) {
        if (field.equals("initialCraftAmount")) {
            ci.cancel();
        }
    }
}
