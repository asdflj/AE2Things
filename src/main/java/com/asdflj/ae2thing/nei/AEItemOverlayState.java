package com.asdflj.ae2thing.nei;

import static codechicken.nei.guihook.GuiContainerManager.drawItem;

import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import appeng.api.AEApi;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.GuiOverlayButton;

public class AEItemOverlayState extends GuiOverlayButton.ItemOverlayState {

    private final boolean isCraftable;

    public AEItemOverlayState(PositionedStack slot, boolean isPresent, boolean isCraftable) {
        super(slot, isPresent);
        this.isCraftable = isCraftable;
    }

    private static final ItemStack PATTERN = AEApi.instance()
        .definitions()
        .items()
        .encodedPattern()
        .maybeStack(1)
        .orNull();

    @Override
    public void draw(GuiOverlayButton.ItemOverlayFormat format) {
        super.draw(format);
        if (this.isPresent && this.isCraftable) {
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glPushMatrix();
            GL11.glTranslatef(0, 0, 200f);
            GL11.glScalef(0.4f, 0.4f, 0.4f);
            drawItem((int) ((this.slot.relx + 10) * 2.5), (int) (this.slot.rely * 2.5), PATTERN);
            GL11.glTranslatef(0, 0, -200f);
            GL11.glPopMatrix();
            GL11.glEnable(GL11.GL_LIGHTING);
        }
    }
}
