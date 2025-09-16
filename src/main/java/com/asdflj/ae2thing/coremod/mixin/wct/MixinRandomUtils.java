package com.asdflj.ae2thing.coremod.mixin.wct;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.p455w0rd.wirelesscraftingterminal.common.utils.RandomUtils;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.asdflj.ae2thing.api.adapter.terminal.item.InventoryPlayerWrapper;

@Mixin(RandomUtils.class)
public class MixinRandomUtils {

    @Inject(method = "getWirelessTerm", at = @At("HEAD"), remap = false, cancellable = true)
    private static void getWirelessTerm(InventoryPlayer playerInv, CallbackInfoReturnable<ItemStack> cir) {
        if (playerInv instanceof InventoryPlayerWrapper w) {
            cir.setReturnValue(w.getTarget());
        }
    }
}
