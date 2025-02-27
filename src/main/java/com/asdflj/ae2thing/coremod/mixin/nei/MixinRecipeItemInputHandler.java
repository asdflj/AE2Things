package com.asdflj.ae2thing.coremod.mixin.nei;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.asdflj.ae2thing.util.Util;

import appeng.client.gui.AEBaseGui;
import codechicken.nei.NEIClientUtils;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.RecipeItemInputHandler;

@Mixin(RecipeItemInputHandler.class)
public abstract class MixinRecipeItemInputHandler {

    @Inject(method = "mouseClicked", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private void ae2thing$mouseClicked(GuiContainer gui, int mousex, int mousey, int button,
        CallbackInfoReturnable<Boolean> cir) {
        if (gui instanceof GuiRecipe<?>g && NEIClientUtils.altKey() && Mouse.getEventButton() == 2) {
            ItemStack item = GuiContainerManager.getStackMouseOver(g);
            if (item == null || item.getItem() == null) {
                return;
            }
            if (g.getFirstScreenGeneral() instanceof AEBaseGui aeBaseGui) {
                Minecraft.getMinecraft()
                    .displayGuiScreen(g.getFirstScreenGeneral());
                Util.setSearchFieldText(aeBaseGui, item.getDisplayName());
                cir.setReturnValue(true);
            }
        }
    }
}
