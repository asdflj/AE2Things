package com.asdflj.ae2thing.coremod.mixin.nei;

import static appeng.client.gui.AEBaseGui.aeRenderItem;
import static codechicken.nei.guihook.GuiContainerManager.getStackMouseOver;
import static com.asdflj.ae2thing.client.render.RenderHelper.canDrawPlus;
import static com.asdflj.ae2thing.client.render.RenderHelper.drawPlus;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.asdflj.ae2thing.nei.TerminalInventoryStateOption;
import com.asdflj.ae2thing.util.Ae2ReflectClient;
import com.asdflj.ae2thing.util.Util;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IDisplayRepo;
import appeng.api.storage.data.IItemList;
import appeng.client.gui.AEBaseGui;
import appeng.client.me.ItemRepo;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.recipe.GuiRecipe;

@Mixin(GuiContainerManager.class)
public abstract class MixinGuiContainerManager {

    @Shadow(remap = false)
    public GuiContainer window;

    @Unique
    private static RenderItem ae2thing$r = new RenderItem();

    private static ItemStack ae2Thing$lastStack = null;
    private static IAEItemStack ae2thing$lastAEStack = null;

    @Inject(
        method = "renderToolTips",
        at = @At(
            value = "INVOKE",
            target = "Lcodechicken/nei/guihook/GuiContainerManager;applyItemCountDetails(Ljava/util/List;Lnet/minecraft/item/ItemStack;)V"),
        remap = false)
    private void ae2thing$renderToolTips(int mousex, int mousey, CallbackInfo ci) {
        if (!TerminalInventoryStateOption.getValue()) return;
        ItemStack stack = getStackMouseOver(this.window);
        if (stack == null) return;
        if (window instanceof GuiRecipe<?>gui && gui.getFirstScreenGeneral() instanceof AEBaseGui g) {
            IDisplayRepo repo = Util.getDisplayRepo(g);
            if (repo == null) return;
            IItemList<IAEItemStack> list = Ae2ReflectClient.getList((ItemRepo) repo);
            IAEItemStack item = list.findPrecise(
                ae2Thing$lastStack != null && Platform.isSameItem(ae2Thing$lastStack, stack)
                    && ae2thing$lastAEStack != null ? ae2thing$lastAEStack : AEItemStack.create(stack));
            if (item != null) {
                ae2thing$render(item, mousex - 8, mousey - 40 < 0 ? mousey + 40 : mousey - 40);
                ae2thing$lastAEStack = item;
                ae2Thing$lastStack = stack;
            }
        }

    }

    @Unique
    private void ae2thing$render(IAEItemStack item, int x, int y) {
        ItemStack stack = item.getItemStack();
        GL11.glPushMatrix();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glTranslatef(0.0f, 0.0f, 350);
        ae2thing$r.renderItemAndEffectIntoGUI(
            Minecraft.getMinecraft().fontRenderer,
            Minecraft.getMinecraft()
                .getTextureManager(),
            stack,
            x,
            y);
        GL11.glTranslatef(0.0f, 0.0f, 200.0f);
        aeRenderItem.setAeStack(item);
        aeRenderItem.renderItemOverlayIntoGUI(
            Minecraft.getMinecraft().fontRenderer,
            Minecraft.getMinecraft()
                .getTextureManager(),
            stack,
            x,
            y);
        GL11.glTranslatef(0.0f, 0.0f, -350.0f);
        if (item.isCraftable() && canDrawPlus) {
            GL11.glTranslatef(0.0f, 0.0f, 450.0f);
            drawPlus(x, y);
            GL11.glTranslatef(0.0f, 0.0f, -450.0f);
        }
        GL11.glPopMatrix();
    }
}
