package com.asdflj.ae2thing.coremod.mixin.ae;

import static appeng.container.slot.SlotRestrictedInput.PlacableItemType.VIEW_CELL;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import appeng.container.slot.AppEngSlot;
import appeng.container.slot.SlotRestrictedInput;
import appeng.items.storage.ItemViewCell;

@Mixin(SlotRestrictedInput.class)
public abstract class MixinSlotRestrictedInput extends AppEngSlot {

    @Shadow(remap = false)
    @Final
    private SlotRestrictedInput.PlacableItemType which;

    public MixinSlotRestrictedInput(IInventory inv, int idx, int x, int y) {
        super(inv, idx, x, y);
    }

    @Inject(
        method = "isItemValid",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/api/definitions/IDefinitions;items()Lappeng/api/definitions/IItems;",
            shift = At.Shift.AFTER),
        remap = false,
        cancellable = true)
    public void isItemValid(ItemStack i, CallbackInfoReturnable<Boolean> cir) {
        if (this.which == VIEW_CELL && i != null && i.getItem() instanceof ItemViewCell) {
            cir.setReturnValue(true);
        }
    }
}
