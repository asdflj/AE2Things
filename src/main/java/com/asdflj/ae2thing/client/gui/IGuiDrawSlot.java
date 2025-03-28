package com.asdflj.ae2thing.client.gui;

import static appeng.client.gui.AEBaseGui.aeRenderItem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.Fluid;

import org.lwjgl.opengl.GL11;

import com.asdflj.ae2thing.client.gui.container.slot.SlotPatternFake;
import com.asdflj.ae2thing.client.render.ISlotRender;
import com.asdflj.ae2thing.client.render.SlotRender;
import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.mitchej123.hodgepodge.textures.IPatchedTextureAtlasSprite;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.AEBaseGui;
import appeng.client.me.SlotME;
import appeng.container.slot.SlotInaccessible;
import appeng.container.slot.SlotPlayerHotBar;
import appeng.container.slot.SlotPlayerInv;
import appeng.util.item.AEItemStack;

public interface IGuiDrawSlot {

    default boolean drawSlot(Slot slot) {
        ItemStack drawStack = slot.getStack();
        if (drawStack == null || drawStack.getItem() == null) return true;
        IAEItemStack stack;
        boolean display = false;
        if (slot instanceof SlotME) {
            stack = ((SlotME) slot).getAEStack();
        } else if (slot instanceof SlotInaccessible) {
            stack = AEItemStack.create(drawStack);
            drawStack.stackSize = 0;
            ((SlotInaccessible) slot).setDisplay(true);
            display = true;
        } else if (slot instanceof SlotPatternFake) {
            stack = ((SlotPatternFake) slot).getAEStack();
        } else if (slot instanceof SlotPlayerInv || slot instanceof SlotPlayerHotBar) {
            stack = AEItemStack.create(drawStack);
        } else {
            return true;
        }
        if (stack == null || stack.getItem() == null) {
            return true;
        }
        for (ISlotRender slotRender : SlotRender.instance()
            .getRenders()) {
            if (slotRender.get()
                .test(slot)) {
                if (!slotRender.drawSlot(slot, stack, this, display)) {
                    return false;
                }
            }
        }
        return true;
    }

    default void renderStackSize(boolean display, IAEItemStack stack, Slot slot) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!display) {
            GL11.glTranslatef(0.0f, 0.0f, 200.0f);
            aeRenderItem.renderItemOverlayIntoGUI(
                mc.fontRenderer,
                mc.getTextureManager(),
                stack.getItemStack(),
                slot.xDisplayPosition,
                slot.yDisplayPosition);
            GL11.glTranslatef(0.0f, 0.0f, -200.0f);
        }
    }

    default FontRenderer getFontRender() {
        return Minecraft.getMinecraft().fontRenderer;
    }

    default void drawWidget(int posX, int posY, Fluid fluid) {
        if (fluid == null) return;
        IIcon icon = fluid.getIcon();
        if (icon == null) return;

        if (ModAndClassUtil.HODGEPODGE && icon instanceof IPatchedTextureAtlasSprite) {
            ((IPatchedTextureAtlasSprite) icon).markNeedsAnimationUpdate();
        }

        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        GL11.glTranslatef(0f, 0f, 100.0f);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor3f(
            (fluid.getColor() >> 16 & 0xFF) / 255.0F,
            (fluid.getColor() >> 8 & 0xFF) / 255.0F,
            (fluid.getColor() & 0xFF) / 255.0F);
        getAEBaseGui().drawTexturedModelRectFromIcon(posX, posY, fluid.getIcon(), 16, 16);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor3f(1, 1, 1);
        GL11.glTranslatef(0.0f, 0.0f, -100.0f);
    }

    AEBaseGui getAEBaseGui();

    float getzLevel();

}
