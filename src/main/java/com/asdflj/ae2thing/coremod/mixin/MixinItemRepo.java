package com.asdflj.ae2thing.coremod.mixin;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.client.gui.IWidgetGui;
import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.asdflj.ae2thing.util.TheUtil;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.client.me.ItemRepo;

@Mixin(ItemRepo.class)
public abstract class MixinItemRepo {

    @Shadow(remap = false)
    @Final
    private ArrayList<ItemStack> dsp;

    @Shadow(remap = false)
    @Final
    private ArrayList<IAEItemStack> view;

    @Shadow(remap = false)
    @Final
    private IItemList<IAEItemStack> list;

    private void setAsEmpty(int i) {
        this.view.add(i, null);
        this.dsp.add(i, null);
    }

    private final Minecraft mc = Minecraft.getMinecraft();

    @Redirect(
        method = "updateView",
        at = @At(value = "INVOKE", target = "Ljava/util/ArrayList;add(Ljava/lang/Object;)Z"),
        remap = false)
    public boolean add(ArrayList<Object> view, Object o) {
        GuiScreen gui = mc.currentScreen;
        if (gui instanceof IWidgetGui || (ModAndClassUtil.THE && TheUtil.isTerminal())) {
            return view.add(o);
        } else if (o instanceof IAEItemStack is && AE2ThingAPI.instance()
            .getPinnedItems()
            .contains(is)) {
                return false;
            } else {
                return view.add(o);
            }
    }

    @Inject(method = "updateView", at = @At(value = "TAIL"), remap = false)
    public void update(CallbackInfo ci) {
        GuiScreen gui = mc.currentScreen;
        if (gui instanceof IWidgetGui || (ModAndClassUtil.THE && TheUtil.isTerminal())) {
            return;
        }
        final List<IAEItemStack> pinItems = AE2ThingAPI.instance()
            .getPinnedItems();
        if (pinItems.isEmpty()) {
            return;
        }

        for (int i = 0; i < AE2ThingAPI.maxPinSize; i++) {
            if (i >= pinItems.size()) {
                this.setAsEmpty(i);
                continue;
            }
            IAEItemStack is = pinItems.get(i);
            IAEItemStack item = this.list.findPrecise(is);
            if (item != null) {
                this.view.add(i, item);
                this.dsp.add(i, item.getItemStack());
                continue;
            }
            this.setAsEmpty(i);
        }
    }
}
