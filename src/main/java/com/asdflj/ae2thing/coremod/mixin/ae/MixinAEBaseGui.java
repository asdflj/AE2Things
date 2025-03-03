package com.asdflj.ae2thing.coremod.mixin.ae;

import static com.asdflj.ae2thing.client.render.RenderHelper.canDrawPlus;
import static com.asdflj.ae2thing.client.render.RenderHelper.drawPlus;

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

    @Inject(
        method = "drawGuiContainerBackgroundLayer",
        at = @At(value = "INVOKE", target = "Lappeng/client/gui/AEBaseGui;drawBG(IIII)V", shift = At.Shift.AFTER))
    @SuppressWarnings({ "unchecked" })
    private void drawPin(float f, int x, int y, CallbackInfo ci) {
        if (!AE2ThingAPI.instance()
            .terminal()
            .isPinTerminal(this)) return;
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

    @Inject(method = "drawAESlot", at = @At("HEAD"), remap = false)
    private void drawAESlot(Slot slotIn, CallbackInfo ci) {
        if (canDrawPlus && slotIn instanceof SlotME slotME && slotME.getHasStack()) {
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

    private void bindTexture() {
        final ResourceLocation loc = AE2Thing.resource("textures/gui/pinned.png");
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(loc);
    }
}
