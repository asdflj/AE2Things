package com.asdflj.ae2thing.client.render;

import static appeng.client.gui.AEBaseGui.aeRenderItem;
import static net.minecraft.client.gui.Gui.drawRect;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.IntBuffer;
import java.nio.file.FileAlreadyExistsException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.event.ClickEvent;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.Fluid;

import org.apache.commons.io.FileUtils;
import org.lwjgl.BufferUtils;
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
import appeng.api.storage.data.IItemList;
import appeng.client.me.SlotME;
import appeng.core.AELog;
import appeng.core.localization.GuiColors;
import appeng.core.localization.GuiText;
import appeng.util.ColorPickHelper;
import appeng.util.Platform;
import appeng.util.ReadableNumberConverter;
import appeng.util.RoundHelper;

public class RenderHelper {

    public static boolean canDrawPlus = false;
    private static Color color;
    private static long lastRunTime;
    public static long interval = 30;
    private static final int ROW_HEIGHT = 23;
    private static final int ROW_SIZE = 9;
    private static final int SCREENSHOT_ZOOM = 2;
    public static RenderItem itemRender = new RenderItem();
    private static final DateTimeFormatter SCREENSHOT_DATE_FORMAT = DateTimeFormatter
        .ofPattern("yyyy-MM-dd_HH.mm.ss", Locale.ROOT);
    private static final Minecraft mc = Minecraft.getMinecraft();

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

    private static void drawCraftingItemState(IAEItemStack refStack, IAEItemStack stored, IAEItemStack pendingStack,
        IAEItemStack missingStack, int x, int y) {
        final FontRenderer fontRendererObj = mc.fontRenderer;
        final int xo = -12;
        final int yo = 4;
        final int sectionLength = 32 * SCREENSHOT_ZOOM;
        final int offY = ROW_HEIGHT;
        GL11.glPushMatrix();
        GL11.glScaled(0.5, 0.5, 0.5);

        int lines = 0;

        if (stored != null && stored.getStackSize() > 0) {
            lines++;
            if (missingStack == null && pendingStack == null) {
                lines++;
            }
        }
        if (missingStack != null && missingStack.getStackSize() > 0) {
            lines++;
        }
        if (pendingStack != null && pendingStack.getStackSize() > 0) {
            lines += 2;
        }

        final int negY = ((lines - 1) * 5) / 2;
        int downY = 0;

        if (stored != null && stored.getStackSize() > 0) {
            String str = GuiText.FromStorage.getLocal() + ": "
                + ReadableNumberConverter.INSTANCE.toWideReadableForm(stored.getStackSize());
            final int w = 4 + fontRendererObj.getStringWidth(str);
            fontRendererObj.drawString(
                str,
                (int) ((x * (1 + sectionLength) + xo + sectionLength - 19 - (w * 0.5)) * 2),
                (y * offY + yo + 6 - negY + downY) * 2,
                GuiColors.CraftConfirmFromStorage.getColor());

            downY += 5;
        }

        boolean red = false;
        if (missingStack != null && missingStack.getStackSize() > 0) {
            String str = GuiText.Missing.getLocal() + ": "
                + ReadableNumberConverter.INSTANCE.toWideReadableForm(missingStack.getStackSize());
            final int w = 4 + fontRendererObj.getStringWidth(str);
            fontRendererObj.drawString(
                str,
                (int) ((x * (1 + sectionLength) + xo + sectionLength - 19 - (w * 0.5)) * 2),
                (y * offY + yo + 6 - negY + downY) * 2,
                GuiColors.CraftConfirmMissing.getColor());

            red = true;
            downY += 5;
        }

        if (pendingStack != null && pendingStack.getStackSize() > 0) {
            String str = GuiText.ToCraft.getLocal() + ": "
                + ReadableNumberConverter.INSTANCE.toWideReadableForm(pendingStack.getStackSize());
            int w = 4 + fontRendererObj.getStringWidth(str);
            fontRendererObj.drawString(
                str,
                (int) ((x * (1 + sectionLength) + xo + sectionLength - 19 - (w * 0.5)) * 2),
                (y * offY + yo + 6 - negY + downY) * 2,
                GuiColors.CraftConfirmToCraft.getColor());

            downY += 5;
            str = GuiText.ToCraftRequests.getLocal() + ": "
                + ReadableNumberConverter.INSTANCE.toWideReadableForm(pendingStack.getCountRequestableCrafts());
            w = 4 + fontRendererObj.getStringWidth(str);
            fontRendererObj.drawString(
                str,
                (int) ((x * (1 + sectionLength) + xo + sectionLength - 19 - (w * 0.5)) * 2),
                (y * offY + yo + 6 - negY + downY) * 2,
                GuiColors.CraftConfirmToCraft.getColor());

        }

        if (stored != null && stored.getStackSize() > 0 && missingStack == null && pendingStack == null) {
            String str = GuiText.FromStoragePercent.getLocal() + ": "
                + RoundHelper.toRoundedFormattedForm(stored.getUsedPercent(), 2)
                + "%";
            int w = 4 + fontRendererObj.getStringWidth(str);
            fontRendererObj.drawString(
                str,
                (int) ((x * (1 + sectionLength) + xo + sectionLength - 19 - (w * 0.5)) * 2),
                (y * offY + yo + 6 - negY + downY) * 2,
                ColorPickHelper.selectColorFromThreshold(stored.getUsedPercent())
                    .getColor());
        }

        GL11.glPopMatrix();
        final int posX = x * (1 + sectionLength) + xo + sectionLength - 19;
        final int posY = y * offY + yo;

        if (red) {
            final int startX = x * (1 + sectionLength) + xo;
            final int startY = posY - 4;
            drawRect(startX, startY, startX + sectionLength, startY + offY, 0x8AFF0000);
        }
        renderAEStack(refStack, posX, posY, 0, false);
    }

    public static void saveScreenshot(List<IAEItemStack> visual, IItemList<IAEItemStack> storage,
        IItemList<IAEItemStack> pending, IItemList<IAEItemStack> missing) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (!OpenGlHelper.isFramebufferEnabled()) {
            AELog.error("Could not save crafting tree screenshot: FBOs disabled/unsupported");
            mc.ingameGUI.getChatGUI()
                .printChatMessage(new ChatComponentTranslation("chat.appliedenergistics2.FBOUnsupported"));
            return;
        }
        try {

            final File screenshotsDir = new File(mc.mcDataDir, "screenshots");
            FileUtils.forceMkdir(screenshotsDir);
            final int maxGlTexSize = GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE) / 2; // Divide by 2 to be safe
            int imgWidth = SCREENSHOT_ZOOM * (ROW_SIZE * 2 * 32);
            int imgHeight = (int) (SCREENSHOT_ZOOM * (Platform.ceilDiv(visual.size(), ROW_SIZE) * ROW_HEIGHT));
            // Make sure the image can be actually allocated, worst case it'll be cropped
            while ((long) imgWidth * (long) imgHeight >= (long) Integer.MAX_VALUE / 4) {
                if (imgWidth > imgHeight) {
                    imgWidth /= 2;
                } else {
                    imgHeight /= 2;
                }
            }

            final int fbWidth = Math.min(imgWidth, maxGlTexSize);
            final int fbHeight = Math.min(imgHeight, maxGlTexSize);
            final BufferedImage outputImg = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);
            final IntBuffer downloadBuffer = BufferUtils.createIntBuffer(fbWidth * fbHeight);
            GL11.glPushMatrix();
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            final Framebuffer fb = new Framebuffer(fbWidth, fbHeight, true);
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glLoadIdentity();
            GL11.glOrtho(0, fbWidth / (float) SCREENSHOT_ZOOM, fbHeight / (float) SCREENSHOT_ZOOM, 0, 1000, 3000);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glLoadIdentity();
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            try {
                fb.bindFramebuffer(true);
                final int xStart = 0;
                final int yStart = 0;
                final int xChunkSize = imgWidth - xStart;
                final int yChunkSize = imgHeight - yStart;
                GL11.glPushMatrix();
                GL11.glTranslatef(0, 0, -2000.0f);
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
                for (int i = 0; i < visual.size(); i++) {
                    final IAEItemStack refStack = visual.get(i);
                    drawCraftingItemState(
                        refStack,
                        storage.findPrecise(refStack),
                        pending.findPrecise(refStack),
                        missing.findPrecise(refStack),
                        (i % ROW_SIZE),
                        i / ROW_SIZE);
                }
                GL11.glPopMatrix();
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, fb.framebufferTexture);
                GL11.glGetTexImage(
                    GL11.GL_TEXTURE_2D,
                    0,
                    GL12.GL_BGRA,
                    GL12.GL_UNSIGNED_INT_8_8_8_8_REV,
                    downloadBuffer);
                for (int y = 0; y < yChunkSize; y++) {
                    for (int x = 0; x < xChunkSize; x++) {
                        outputImg.setRGB(x + xStart, y + yStart, downloadBuffer.get((fbHeight - 1 - y) * fbWidth + x));
                    }
                }
            } finally {
                fb.deleteFramebuffer();
                GL11.glViewport(0, 0, mc.displayWidth, mc.displayHeight);
            }
            GL11.glPopAttrib();
            GL11.glPopMatrix();

            final String date = SCREENSHOT_DATE_FORMAT.format(LocalDateTime.now());
            String filename = String.format("%s-ae2.png", date);
            File outFile = new File(screenshotsDir, filename);
            for (int i = 1; outFile.exists() && i < 99; i++) {
                filename = String.format("%s-ae2-%d.png", date, i);
                outFile = new File(screenshotsDir, filename);
            }
            if (outFile.exists()) {
                throw new FileAlreadyExistsException(filename);
            }
            ImageIO.write(outputImg, "png", outFile);

            AELog.info("Saved crafting list screenshot to %s", filename);
            ChatComponentText chatLink = new ChatComponentText(filename);
            chatLink.getChatStyle()
                .setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, outFile.getAbsolutePath()));
            chatLink.getChatStyle()
                .setUnderlined(Boolean.valueOf(true));
            mc.ingameGUI.getChatGUI()
                .printChatMessage(new ChatComponentTranslation("screenshot.success", chatLink));
        } catch (Exception e) {
            AELog.warn(e, "Could not save crafting list screenshot");
            mc.ingameGUI.getChatGUI()
                .printChatMessage(new ChatComponentTranslation("screenshot.failure", e.getMessage()));
        }
    }

    public static void renderAEStack(IAEStack<?> stack, int x, int y, float z) {
        renderAEStack(stack, x, y, z, true);
    }

    public static void renderItemStack(ItemStack stack, int x, int y, float z) {
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
            stack,
            x,
            y);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
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
