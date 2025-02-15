package com.asdflj.ae2thing.client.gui;

import static appeng.client.gui.AEBaseGui.aeRenderItem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import com.asdflj.ae2thing.client.gui.container.slot.SlotPatternFake;
import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.crossmod.thaumcraft.AspectRender;
import com.glodblock.github.crossmod.thaumcraft.AspectUtil;
import com.mitchej123.hodgepodge.textures.IPatchedTextureAtlasSprite;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.AEBaseGui;
import appeng.client.me.SlotME;
import appeng.container.slot.SlotInaccessible;
import appeng.util.item.AEItemStack;

public interface IGuiDrawSlot {

    default boolean drawSlot(Slot slot) {
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack drawStack = slot.getStack();
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
        } else {
            return true;
        }

        if (stack == null || stack.getItem() == null
            || !((stack.getItem() instanceof ItemFluidDrop) || (stack.getItem() instanceof ItemFluidPacket)))
            return true;

        FluidStack fluidStack;
        if (stack.getItem() instanceof ItemFluidPacket && !(slot instanceof SlotME)) {
            fluidStack = ItemFluidPacket.getFluidStack(stack);
            if (fluidStack == null || fluidStack.amount <= 0) {
                return true;
            }
            this.getAEBaseGui()
                .drawMCSlot(slot);
            IAEItemStack fake = stack.copy();
            fake.setStackSize(fluidStack.amount);
            aeRenderItem.setAeStack(fake);
            renderStackSize(display, stack, slot);
            return false;
        } else if (stack.getItem() instanceof ItemFluidDrop) {
            fluidStack = ItemFluidDrop.getFluidStack(slot.getStack());
            if (fluidStack == null || fluidStack.getFluid() == null) return true;
            if (ModAndClassUtil.THE && AspectUtil.isEssentiaGas(fluidStack)) {
                GL11.glTranslatef(0.0f, 0.0f, 150.0f);
                AspectRender.drawAspect(
                    mc.thePlayer,
                    slot.xDisplayPosition,
                    slot.yDisplayPosition,
                    this.getzLevel(),
                    AspectUtil.getAspectFromGas(fluidStack),
                    fluidStack.amount <= 0 ? 1 : fluidStack.amount);
                GL11.glTranslatef(0.0f, 0.0f, -150.0f);
                IAEItemStack gas = stack.copy()
                    .setStackSize(stack.getStackSize() / AspectUtil.R);
                aeRenderItem.setAeStack(gas);
                renderStackSize(display, stack, slot);
            } else {
                this.drawWidget(slot.xDisplayPosition, slot.yDisplayPosition, fluidStack.getFluid());
                aeRenderItem.setAeStack(stack);
                renderStackSize(display, stack, slot);
            }
            return false;
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
