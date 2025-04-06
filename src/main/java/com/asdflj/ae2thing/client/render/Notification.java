package com.asdflj.ae2thing.client.render;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.client.event.NotificationEvent;

public class Notification {

    public static Notification INSTANCE = new Notification();

    private static final List<NotificationEvent> events = new ArrayList<>();
    private static NotificationEvent currentEvent = null;
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static long startTime = 0;
    private static long endTime = 0;
    private static final int DELAY = 3000;
    private static final int FADE_IN = 1000;
    private static final int WIDTH = 130;
    private static final int TOP = 6;

    public void clear() {
        events.clear();
    }

    public void add(NotificationEvent event) {
        events.add(event);
    }

    public void draw() {
        if (currentEvent == null && !events.isEmpty()) {
            currentEvent = events.remove(0);
            startTime = System.currentTimeMillis();
            endTime = startTime + DELAY;
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            Minecraft.getMinecraft().theWorld
                .playSound((int) player.posX, (int) player.posY, (int) player.posZ, "random.levelup", 0.25f, 1, false);
        } else if (currentEvent != null && endTime > 0) {
            drawNotification(currentEvent);
        }
    }

    private void drawNotification(NotificationEvent event) {
        long currentTime = System.currentTimeMillis();
        if (currentTime > endTime) {
            currentEvent = null;
            endTime = 0;
            startTime = 0;
            return;
        }
        ScaledResolution resolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int x;
        if (startTime + FADE_IN < currentTime) {
            x = resolution.getScaledWidth() - WIDTH;
            drawContent(event, x);
        } else {
            double percent = Math.round((((double) (currentTime - startTime)) / FADE_IN) * 100.0) / 100.0;
            x = resolution.getScaledWidth() - (int) (WIDTH * percent);
            drawContent(event, x);
        }
    }

    private void drawContent(NotificationEvent event, int x) {
        final ResourceLocation loc = AE2Thing.resource("textures/gui/notification.png");
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(loc);
        Gui.func_146110_a(x - 26, 0, 0, 0, 156, 35, 256, 256);
        RenderHelper.renderItemStack(event.getItem(), x - 18, TOP + 2, 150);
        mc.fontRenderer.drawStringWithShadow(getDrawString(event.getTile()), x, TOP, 0xffffff);
        mc.fontRenderer.drawStringWithShadow((event.getContent()), x, TOP + mc.fontRenderer.FONT_HEIGHT + 4, 0xffffff);

    }

    private String getDrawString(String text) {
        if (mc.fontRenderer.getStringWidth(text) > WIDTH) {
            StringBuilder builder = new StringBuilder();
            int w = 0;
            for (int i = 0; i < text.toCharArray().length; i++) {
                char s = text.charAt(i);
                int charWidth = mc.fontRenderer.getCharWidth(s);
                if (w + charWidth < WIDTH) {
                    w += charWidth;
                    builder.append(s);
                } else {
                    break;
                }
            }
            return builder.toString();
        }
        return text;
    }

}
