package com.asdflj.ae2thing.coremod.mixin.ae;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.client.render.RenderHelper;
import com.asdflj.ae2thing.network.CPacketTerminalBtns;
import com.asdflj.ae2thing.util.NameConst;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.implementations.GuiCraftConfirm;
import appeng.client.gui.widgets.GuiAeButton;
import appeng.client.gui.widgets.GuiSimpleImgButton;
import appeng.util.item.ItemList;

@Mixin(GuiCraftConfirm.class)
public abstract class MixinGuiCraftConfirm extends AEBaseGui {

    @Shadow(remap = false)
    private GuiButton start;

    @Shadow(remap = false)
    @Final
    private IItemList<IAEItemStack> storage;
    @Shadow(remap = false)
    @Final
    private IItemList<IAEItemStack> pending;
    @Shadow(remap = false)
    @Final
    private IItemList<IAEItemStack> missing;
    @Shadow(remap = false)
    @Final
    private List<IAEItemStack> visual;
    @Shadow(remap = false)
    private GuiSimpleImgButton takeScreenshot;
    @Shadow(remap = false)
    private GuiCraftConfirm.DisplayMode displayMode;
    private GuiAeButton replan = null;
    private boolean clickStart = false;

    public MixinGuiCraftConfirm(Container container) {
        super(container);
    }

    @Inject(method = "actionPerformed", at = @At(value = "HEAD"), cancellable = true)
    private void actionPerformed(GuiButton btn, CallbackInfo ci) {
        if (btn == start) {
            clickStart = true;
        } else if (btn == replan) {
            clickStart = false;
            start.enabled = false;
            replan.visible = false;
            ((ItemList) this.storage).clear();
            ((ItemList) this.pending).clear();
            ((ItemList) this.missing).clear();
            this.visual.clear();
            AE2Thing.proxy.netHandler.sendToServer(new CPacketTerminalBtns("GuiCraftConfirm.replan", true));
        } else if (btn == takeScreenshot && this.displayMode == GuiCraftConfirm.DisplayMode.LIST) {
            RenderHelper.saveScreenshot(this.visual, this.storage, this.pending, this.missing);
            ci.cancel();
        }
    }

    @Inject(method = "initGui", at = @At("TAIL"))
    public void initGui(CallbackInfo ci) {
        this.buttonList.add(
            replan = new GuiAeButton(
                0,
                start.xPosition,
                start.yPosition,
                start.width,
                start.height,
                I18n.format(NameConst.GUI_BUTTON_REPLAN),
                ""));
        this.replan.visible = false;
    }

    @Inject(method = "drawScreen", at = @At(value = "INVOKE", target = "Lappeng/client/gui/AEBaseGui;drawScreen(IIF)V"))
    public void drawScreen(int mouseX, int mouseY, float btn, CallbackInfo ci) {
        this.takeScreenshot.visible = true;
    }

    @Inject(method = "drawFG", at = @At("HEAD"), remap = false)
    public void drawFG(CallbackInfo ci) {
        try {
            if (clickStart || !start.enabled) {
                replan.visible = true;
                start.visible = false;
            } else {
                replan.visible = false;
                start.visible = true;
            }
        } catch (Exception ignored) {}

    }
}
