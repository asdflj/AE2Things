package com.asdflj.ae2thing.coremod.mixin;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.client.gui.IWidgetGui;
import com.asdflj.ae2thing.network.CPacketNetworkCraftingItems;
import com.glodblock.github.client.gui.GuiFluidMonitor;

import appeng.client.gui.AEBaseGui;
import appeng.client.me.InternalSlotME;

@Mixin(value = AEBaseGui.class)
public abstract class MixinAEBaseGui extends GuiScreen {

    @Shadow(remap = false)
    public abstract int getGuiLeft();

    @Shadow(remap = false)
    public abstract int getGuiTop();

    @Shadow(remap = false)
    protected abstract List<InternalSlotME> getMeSlots();

    @Shadow
    public abstract void initGui();

    @Inject(
        method = "drawGuiContainerBackgroundLayer",
        at = @At(value = "INVOKE", target = "Lappeng/client/gui/AEBaseGui;drawBG(IIII)V", shift = At.Shift.AFTER),
        remap = false)
    @SuppressWarnings({ "unchecked" })
    private void drawPin(float f, int x, int y, CallbackInfo ci) {
        if (this.getMeSlots()
            .isEmpty()
            || AE2ThingAPI.instance()
                .getPinnedItems()
                .isEmpty())
            return;
        if (Minecraft.getMinecraft().currentScreen instanceof GuiFluidMonitor || this instanceof IWidgetGui) return;
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.bindTexture();
        this.drawTexturedModalRect(this.getGuiLeft() + 8, this.getGuiTop() + 17, 0, 0, 195, 18);
    }

    private void bindTexture() {
        final ResourceLocation loc = AE2Thing.resource("textures/gui/pinned.png");
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(loc);
    }

    @Inject(method = "initGui", at = @At("HEAD"))
    private void initGui(CallbackInfo ci) {
        CPacketNetworkCraftingItems p = new CPacketNetworkCraftingItems();
        AE2Thing.proxy.netHandler.sendToServer(p);
    }
}
