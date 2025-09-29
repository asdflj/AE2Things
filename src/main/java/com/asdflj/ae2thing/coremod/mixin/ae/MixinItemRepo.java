package com.asdflj.ae2thing.coremod.mixin.ae;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
import com.asdflj.ae2thing.client.me.AdvItemRepo;
import com.asdflj.ae2thing.client.me.IDisplayRepoExtend;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IDisplayRepo;
import appeng.api.storage.data.IItemList;
import appeng.client.me.ItemRepo;

@Mixin(ItemRepo.class)
public abstract class MixinItemRepo implements IDisplayRepo, IDisplayRepoExtend {

    @Shadow(remap = false)
    @Final
    private ArrayList<ItemStack> dsp;

    @Shadow(remap = false)
    @Final
    private ArrayList<IAEItemStack> view;

    @Shadow(remap = false)
    @Final
    private IItemList<IAEItemStack> list;

    @Shadow(remap = false)
    private boolean paused;

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
        return addView(view, o);
    }

    private boolean addView(ArrayList<Object> view, Object o) {
        GuiScreen gui = mc.currentScreen;
        if (gui == null) return view.add(o);;
        if (!AE2ThingAPI.instance()
            .terminal()
            .isPinTerminal(gui)) {
            return view.add(o);
        } else if ((o instanceof IAEItemStack is && AE2ThingAPI.instance()
            .getPinned()
            .isPinnedItem(is))) {
                return false;
            } else {
                return view.add(o);
            }
    }

    @Redirect(
        method = "addEntriesToView",
        at = @At(value = "INVOKE", target = "Ljava/util/ArrayList;add(Ljava/lang/Object;)Z"),
        remap = false,
        require = 0)
    public boolean addEntriesToView(ArrayList<Object> view, Object o) {
        return addView(view, o);
    }

    @Inject(method = "updateView", at = @At(value = "HEAD"), remap = false)
    public void updateViewHead(CallbackInfo ci) {
        if (((Object) this) instanceof AdvItemRepo repo) {
            if (!repo.hasCache()) {
                repo.getLock()
                    .lock();
                viewFilter();
                repo.getLock()
                    .unlock();
            }
        } else {
            viewFilter();
        }
    }

    private void viewFilter() {
        List<IAEItemStack> list = this.view.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        this.view.clear();
        this.view.addAll(list);
    }

    @Inject(method = "updateView", at = @At(value = "TAIL"), remap = false)
    public void updateViewTail(CallbackInfo ci) {
        GuiScreen gui = mc.currentScreen;
        if (gui == null) return;
        if (!AE2ThingAPI.instance()
            .terminal()
            .isPinTerminal(gui)) {
            return;
        }
        final List<IAEItemStack> pinItems = AE2ThingAPI.instance()
            .getPinned()
            .getSortedPinnedItems();
        if (pinItems.isEmpty()) {
            return;
        }

        for (int i = 0; i < AE2ThingAPI.instance()
            .getPinned()
            .getMaxPinSize(); i++) {
            if (i >= pinItems.size()) {
                this.setAsEmpty(i);
                continue;
            }
            IAEItemStack is = pinItems.get(i);
            IAEItemStack item = this.list.findPrecise(is);
            Set<IAEItemStack> itemSet = new HashSet<>(this.view);
            if (item != null) {
                if (!itemSet.contains(item)) {
                    this.view.add(i, item);
                    this.dsp.add(i, item.getItemStack());
                }
                continue;
            }
            this.setAsEmpty(i);
        }
    }

    @Override
    public void setAdvRepoPause(boolean pause) {
        this.paused = pause;
    }
}
