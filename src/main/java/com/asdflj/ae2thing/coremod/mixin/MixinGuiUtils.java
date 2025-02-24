package com.asdflj.ae2thing.coremod.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.client.gui.GuiWirelessDualInterfaceTerminal;
import com.asdflj.ae2thing.client.gui.container.ContainerWirelessDualInterfaceTerminal;
import com.github.vfyjxf.nee.utils.GuiUtils;

import appeng.helpers.IContainerCraftingPacket;

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

    @Inject(method = "isCraftingSlot", at = @At("TAIL"), remap = false, cancellable = true)
    private static void isCraftingSlot(Slot slot, CallbackInfoReturnable<Boolean> cir) {
        Container container = Minecraft.getMinecraft().thePlayer.openContainer;
        if (container instanceof ContainerWirelessDualInterfaceTerminal dualContainer) {
            if (!dualContainer.isCraftingMode()) {
                IContainerCraftingPacket cct = (IContainerCraftingPacket) container;
                IInventory craftMatrix = cct.getInventoryByName(Constants.CRAFTING_EX);
                cir.setReturnValue(craftMatrix.equals(slot.inventory));
                cir.cancel();
            }
        }

    }

    @Inject(method = "isPatternContainer", at = @At("HEAD"), remap = false, cancellable = true)
    private static void isPatternContainer(Container container, CallbackInfoReturnable<Boolean> cir) {
        if (container instanceof ContainerWirelessDualInterfaceTerminal) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(method = "isPatternTerm", at = @At("HEAD"), remap = false, cancellable = true)
    private static void isPatternTerm(GuiScreen guiScreen, CallbackInfoReturnable<Boolean> cir) {
        if (guiScreen instanceof GuiWirelessDualInterfaceTerminal) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}
