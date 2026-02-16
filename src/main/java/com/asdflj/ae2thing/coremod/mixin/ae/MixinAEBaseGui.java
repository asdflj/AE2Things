package com.asdflj.ae2thing.coremod.mixin.ae;

import static com.asdflj.ae2thing.client.render.RenderHelper.canDrawPlus;
import static com.asdflj.ae2thing.client.render.RenderHelper.drawPlus;

import java.util.List;
import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.api.TerminalMenu;
import com.asdflj.ae2thing.client.event.AEGuiCloseEvent;
import com.asdflj.ae2thing.client.render.RenderHelper;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.client.me.InternalSlotME;
import appeng.client.me.SlotME;
import appeng.container.slot.SlotPlayerHotBar;
import appeng.container.slot.SlotPlayerInv;
import codechicken.nei.recipe.StackInfo;

@Mixin(value = AEBaseGui.class)
public abstract class MixinAEBaseGui extends GuiContainer {

    public MixinAEBaseGui(Container container) {
        super(container);
    }

    @Shadow(remap = false)
    public abstract int getGuiLeft();

    @Shadow(remap = false)
    public abstract int getGuiTop();

    @Shadow(remap = false)
    protected abstract List<InternalSlotME> getMeSlots();

    @Shadow(remap = false)
    protected abstract List<Slot> getInventorySlots();

    @Shadow(remap = false)
    protected abstract GuiScrollbar getScrollBar();

    @Inject(
        method = "drawGuiContainerBackgroundLayer",
        at = @At(
            value = "INVOKE",
            remap = false,
            target = "Lappeng/client/gui/AEBaseGui;drawBG(IIII)V",
            shift = At.Shift.AFTER))
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
        if (slot.isPresent() && this.getScrollBar() != null
            && this.getScrollBar()
                .getCurrentScroll() == 0) {
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

    @Inject(method = "handleMouseClick", at = @At(value = "HEAD"), cancellable = true)
    protected void handleMouseClick(Slot slot, int slotIdx, int ctrlDown, int mouseButton, CallbackInfo ci) {
        if (ctrlDown == 1 && mouseButton == 0
            && (slot instanceof SlotPlayerInv || slot instanceof SlotPlayerHotBar)
            && slot.getHasStack()) {
            ItemStack item = slot.getStack();
            TerminalMenu menu = new TerminalMenu();
            for (int i = 0; i < menu.getItems()
                .size(); i++) {
                ItemStack term = menu.getItems()
                    .get(i);
                if (StackInfo.equalItemAndNBT(term, item, true)) {
                    menu.openTerminal(i);
                    ci.cancel();
                    break;
                }
            }

        }
    }

    @Inject(method = "onGuiClosed", at = @At(value = "HEAD"))
    public void onGuiClosed(CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new AEGuiCloseEvent((AEBaseGui) (Object) this));
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

    @Inject(method = "handleMouseInput", at = @At("HEAD"))
    public void handleMouseInput(CallbackInfo ci) {
        if (this.getScrollBar() != null) {
            if (!Mouse.isButtonDown(0)) {
                ((AccessorGuiScrollbar) this.getScrollBar()).setIsLatestClickOnScrollbar(false);
            }
        }
    }
}
