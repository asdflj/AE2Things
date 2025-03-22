package com.asdflj.ae2thing.coremod.mixin.ae;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.asdflj.ae2thing.common.storage.ToggleableViewCellPartitionList;
import com.llamalad7.mixinextras.sugar.Local;

import appeng.api.storage.data.IAEItemStack;
import appeng.items.storage.ItemViewCell;
import appeng.util.prioitylist.IPartitionList;
import appeng.util.prioitylist.MergedPriorityList;

@Mixin(ItemViewCell.class)
public abstract class MixinItemViewCell {

    @Inject(
        method = "createFilter",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/api/storage/ICellWorkbenchItem;getOreFilter(Lnet/minecraft/item/ItemStack;)Ljava/lang/String;",
            shift = At.Shift.AFTER),
        remap = false)
    private static void createFilter(ItemStack[] list, CallbackInfoReturnable<IPartitionList<IAEItemStack>> cir,
        @Local MergedPriorityList<IAEItemStack> myMergedList, @Local final ItemStack currentViewCell) {
        myMergedList.addNewList(new ToggleableViewCellPartitionList(currentViewCell), true);
    }
}
