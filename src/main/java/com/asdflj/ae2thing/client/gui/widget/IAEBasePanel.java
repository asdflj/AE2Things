package com.asdflj.ae2thing.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

import com.asdflj.ae2thing.AE2Thing;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IAEBasePanel {

    default void bindTextureBack(final String file) {
        final ResourceLocation loc = new ResourceLocation(AE2Thing.MODID, "textures/" + file);
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(loc);
    }

    String getBackground();

    void drawFG(int offsetX, int offsetY, int mouseX, int mouseY);

    void drawBG(int offsetX, int offsetY, int mouseX, int mouseY);

    void drawScreen(int mouseX, int mouseY, float btn);

    void initGui();

    boolean hideItemPanelSlot(int x, int y, int w, int h);

    boolean mouseClicked(int xCoord, int yCoord, int btn);

    void handleMouseClick(Slot slot, int slotIdx, int ctrlDown, int mouseButton);

    void actionPerformed(GuiButton btn);

    void mouseClickMove(final int x, final int y, final int c, final long d);

    boolean mouseWheelEvent(int mouseX, int mouseY, int wheel);
}
