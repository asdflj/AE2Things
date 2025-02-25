package com.asdflj.ae2thing.coremod.mixin.ae;

import java.util.List;
import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.client.render.RenderHelper;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.AEBaseGui;
import appeng.client.me.InternalSlotME;
import appeng.client.me.SlotME;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

@Mixin(value = AEBaseGui.class)
public abstract class MixinAEBaseGui extends GuiScreen {

    @Shadow(remap = false)
    public abstract int getGuiLeft();

    @Shadow(remap = false)
    public abstract int getGuiTop();

    @Shadow(remap = false)
    protected abstract List<InternalSlotME> getMeSlots();

    @Shadow(remap = false)
    protected abstract List<Slot> getInventorySlots();

    private static boolean drawPlus = false;

    @Inject(
        method = { "drawGuiContainerBackgroundLayer", "func_146976_a" },
        at = @At(value = "INVOKE", target = "Lappeng/client/gui/AEBaseGui;drawBG(IIII)V", shift = At.Shift.AFTER),
        remap = false)
    @SuppressWarnings({ "unchecked" })
    private void drawPin(float f, int x, int y, CallbackInfo ci) {
        if (!AE2ThingAPI.instance()
            .terminal()
            .isTerminal(this)) return;
        if (this.getMeSlots()
            .isEmpty()
            || AE2ThingAPI.instance()
                .getPinned()
                .isEmpty())
            return;
        Optional<Slot> slot = this.getInventorySlots()
            .stream()
            .filter(s -> s instanceof SlotME)
            .findFirst();
        if (slot.isPresent()) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.bindTexture();
            this.drawTexturedModalRect(
                this.getGuiLeft() + slot.get().xDisplayPosition - 1,
                this.getGuiTop() + 17,
                0,
                0,
                195,
                18);
        }

    }

    private void drawPlus(int x, int y) {
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

    @Inject(method = "drawAESlot", at = @At("HEAD"), remap = false)
    private void drawAESlot(Slot slotIn, CallbackInfo ci) {
        if (drawPlus && slotIn instanceof SlotME slotME && slotME.getHasStack()) {
            IAEItemStack is = slotME.getAEStack();
            if (is.isCraftable() && is.getStackSize() > 0) {
                int x = slotIn.xDisplayPosition;
                int y = slotIn.yDisplayPosition;
                drawPlus(x, y);
            }
        }
    }

    @Inject(method = "drawAESlot", at = @At("TAIL"), remap = false)
    private void drawAESlotBG(Slot slotIn, CallbackInfo ci) {
        RenderHelper.drawPinnedSlot(slotIn, this);
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
            drawPlus = v < 536;
        } catch (Exception ignored) {
            drawPlus = false;
        }
    }

    private void bindTexture() {
        final ResourceLocation loc = AE2Thing.resource("textures/gui/pinned.png");
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(loc);
    }

    static {
        setCanDrawPlus();
    }
}
