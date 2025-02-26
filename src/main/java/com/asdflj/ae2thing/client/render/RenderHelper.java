package com.asdflj.ae2thing.client.render;

import static net.minecraft.client.gui.Gui.drawRect;

import java.awt.Color;
import java.util.Optional;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Slot;

import org.lwjgl.opengl.GL11;

import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.api.Pinned;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.me.SlotME;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

public class RenderHelper {

    public static boolean canDrawPlus = false;
    private static Color color;
    private static long lastRunTime;
    public static long interval = 30;

    public static void drawPinnedSlot(Slot slotIn, GuiScreen gui) {
        if (!AE2ThingAPI.instance()
            .terminal()
            .isTerminal(gui)) return;
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
                color = getDynamicColor();
                drawSlotBG(x, y);
            }
        }
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

    private static void drawSlotBG(int x, int y) {
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

    private static String getModVersion() {
        Optional<ModContainer> mod = Loader.instance()
            .getActiveModList()
            .stream()
            .filter(
                x -> x.getModId()
                    .equals("appliedenergistics2"))
            .findFirst();
        if (mod.isPresent()) {
            return mod.get()
                .getVersion();
        }
        return "";
    }

    private static void setCanDrawPlus() {
        String version = getModVersion();
        try {
            int v = Integer.valueOf(version.split("-")[2]);
            canDrawPlus = v < 536;
        } catch (Exception ignored) {
            canDrawPlus = false;
        }
    }

    static {
        setCanDrawPlus();
    }
}
