package com.asdflj.ae2thing.client.render;

import static appeng.client.gui.AEBaseGui.aeRenderItem;

import java.util.function.Predicate;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;

import org.lwjgl.opengl.GL11;

import com.asdflj.ae2thing.client.gui.IGuiDrawSlot;
import com.asdflj.ae2thing.client.gui.container.slot.InfusionTerminalSlotPatternFake;
import com.asdflj.ae2thing.common.item.ItemPhial;

import appeng.api.storage.data.IAEItemStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.Thaumcraft;

public class RenderItemPhial implements ISlotRender {

    @Override
    public Predicate<Slot> get() {
        return slot -> slot instanceof InfusionTerminalSlotPatternFake && slot.getStack()
            .getItem() instanceof ItemPhial;
    }

    @Override
    public boolean drawSlot(Slot slot, IAEItemStack stack, IGuiDrawSlot draw, boolean display) {
        if (slot instanceof InfusionTerminalSlotPatternFake) {
            Aspect aspect = ItemPhial.getAspect(stack);
            if (aspect == null) return true;
            boolean isKnown = false;
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            if (player != null) {
                isKnown = canPlayerSeeAspect(player, aspect);
            }
            if (isKnown) {
                // Render aspect
                UtilsFX.drawTag(slot.xDisplayPosition, slot.yDisplayPosition, aspect, 0.0F, 0, 0.0D);
            } else {
                // Render unknown

                // Disable standard lighting
                GL11.glDisable(GL11.GL_LIGHTING);

                // Set the alpha function
                GL11.glAlphaFunc(GL11.GL_GREATER, 0.003921569F);

                // Enable alpha blending
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

                // Render
                UtilsFX.bindTexture("textures/aspects/_unknown.png");
                GL11.glColor4f(0.5F, 0.5F, 0.5F, 0.5F);
                UtilsFX.drawTexturedQuadFull(slot.xDisplayPosition, slot.yDisplayPosition, 0.0D);

                // Restore
                GL11.glEnable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_BLEND);
            }
            aeRenderItem.setAeStack(stack);
            draw.renderStackSize(display, stack, slot);
            return false;

        }
        return true;
    }

    public static boolean canPlayerSeeAspect(@Nonnull final EntityPlayer player, @Nonnull final Aspect aspect) {
        return Thaumcraft.proxy.playerKnowledge.hasDiscoveredAspect(player.getCommandSenderName(), aspect);
    }
}
