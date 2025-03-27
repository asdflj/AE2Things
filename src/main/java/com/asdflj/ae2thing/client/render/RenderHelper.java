package com.asdflj.ae2thing.client.render;

import static appeng.client.gui.AEBaseGui.aeRenderItem;
import static net.minecraft.client.gui.Gui.drawRect;

import java.awt.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.inventory.Slot;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.Fluid;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.api.Pinned;
import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.asdflj.ae2thing.util.Util;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.crossmod.thaumcraft.AspectRender;
import com.glodblock.github.crossmod.thaumcraft.AspectUtil;
import com.mitchej123.hodgepodge.textures.IPatchedTextureAtlasSprite;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.client.me.SlotME;

public class RenderHelper {

    public static boolean canDrawPlus = false;
    private static Color color;
    private static long lastRunTime;
    public static long interval = 30;
    public static RenderItem itemRender = new RenderItem();

    public static void drawPinnedSlot(Slot slotIn, GuiScreen gui) {
        if (!AE2ThingAPI.instance()
            .terminal()
            .isPinTerminal(gui)) return;
        if (slotIn instanceof SlotME slotME && slotME.getHasStack()) {
            int x = slotIn.xDisplayPosition;
            int y = slotIn.yDisplayPosition;
            IAEItemStack item = ((SlotME) slotIn).getAEStack();
            if (!AE2ThingAPI.instance()
                .getPinned()
                .isPinnedItem(item)) return;
            Pinned.PinInfo info = AE2ThingAPI.instance()
                .getPinned()
                .getPinInfo(item);
            if (info != null && !info.canPrune) {
                updateColorAndDrawItemBorder(x, y);
            }
        }
    }

    public static void renderAEStack(IAEStack<?> stack, int x, int y, float z) {
        renderAEStack(stack, x, y, z, true);
    }

    public static void renderAEStack(IAEStack<?> stack, int x, int y, float z, boolean renderStackSize) {
        if (stack instanceof IAEItemStack itemStack) {
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            GL11.glPushMatrix();
            net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glTranslatef(0f, 0f, z);
            itemRender.renderItemAndEffectIntoGUI(
                Minecraft.getMinecraft().fontRenderer,
                Minecraft.getMinecraft()
                    .getTextureManager(),
                itemStack.getItemStack(),
                x,
                y);
            GL11.glTranslatef(0f, 0f, 150);
            if (renderStackSize) {
                drawStackSize(itemStack, x, y);
            }
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        } else if (stack instanceof IAEFluidStack fluidStack) {
            IAEItemStack fluidDrop = ItemFluidDrop.newAeStack(fluidStack);
            if (fluidDrop == null) return;
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            GL11.glPushMatrix();
            net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glTranslatef(0f, 0f, z);
            if (ModAndClassUtil.THE && AspectUtil.isEssentiaGas(fluidStack)) {
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                AspectRender.drawAspect(
                    Minecraft.getMinecraft().thePlayer,
                    x,
                    y,
                    z,
                    AspectUtil.getAspectFromGas(fluidStack.getFluidStack()),
                    fluidStack.getStackSize() <= 0 ? 1 : fluidStack.getStackSize());
                IAEItemStack gas = fluidDrop.copy()
                    .setStackSize(stack.getStackSize() / AspectUtil.R);
                GL11.glTranslatef(0f, 0f, 150f);
                if (renderStackSize) {
                    drawStackSize(gas, x, y);
                }
            } else {
                drawFluid(x, y, fluidStack.getFluid());
                GL11.glTranslatef(0f, 0f, 150f);
                if (renderStackSize) {
                    drawStackSize(fluidDrop, x, y);
                }
            }
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    private static void drawStackSize(IAEItemStack item, int x, int y) {
        aeRenderItem.setAeStack(item);
        aeRenderItem.renderItemOverlayIntoGUI(
            Minecraft.getMinecraft().fontRenderer,
            Minecraft.getMinecraft()
                .getTextureManager(),
            item.getItemStack(),
            x,
            y);
    }

    private static void drawFluid(int posX, int posY, Fluid fluid) {
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
        Minecraft.getMinecraft().currentScreen.drawTexturedModelRectFromIcon(posX, posY, fluid.getIcon(), 16, 16);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor3f(1, 1, 1);
        GL11.glTranslatef(0.0f, 0.0f, -100.0f);
    }

    public static void updateColor() {
        color = getDynamicColor();
    }

    private static Color getDynamicColor() {
        long time = System.currentTimeMillis();
        if (time - lastRunTime >= interval || color == null) {
            lastRunTime = time;
            float hue = (time % 2000) / 2000.0F;
            Color c = Color.getHSBColor(hue, 1.0F, 1.0F);
            return new Color(c.getRed(), c.getGreen(), c.getBlue(), 128);
        } else {
            return color;
        }
    }

    public static void drawItemBorder(int x, int y) {
        if (color == null) return;
        int width = 16;
        int height = 16;
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0f, 0.0f, 250.0f);
        drawRect(x - 1, y - 1, x + width + 1, y, color.getRGB());
        drawRect(x - 1, y + height + 1, x + width + 1, y + height, color.getRGB());
        drawRect(x - 1, y, x, y + height, color.getRGB());
        drawRect(x + width, y, x + width + 1, y + height, color.getRGB());
        GL11.glTranslatef(0.0f, 0.0f, -250.0f);
        GL11.glPopMatrix();
    }

    public static void updateColorAndDrawItemBorder(int x, int y) {
        updateColor();
        drawItemBorder(x, y);
    }

    public static void drawPlus(int x, int y) {
        float startX = x + 0.5f;
        float startY = y + 0.25f;
        float endX = startX + 3f;
        float endY = startY + 3f;
        GL11.glPushMatrix();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glTranslatef(0f, 0f, 250);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(3.0F);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(startX, startY + 1.5f);
        GL11.glVertex2f(endX, startY + 1.5f);
        GL11.glEnd();
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(startX + 1.5f, startY);
        GL11.glVertex2f(startX + 1.5f, endY);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glTranslatef(0f, 0f, -250);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }

    public static void disableStandardItemLighting() {
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_LIGHT0);
        GL11.glDisable(GL11.GL_LIGHT1);
        GL11.glDisable(GL11.GL_COLOR_MATERIAL);
    }

    private static void setCanDrawPlus() {
        canDrawPlus = Util.getAEVersion() < 536;
    }

    static {
        setCanDrawPlus();
    }
}
