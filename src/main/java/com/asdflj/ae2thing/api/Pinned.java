package com.asdflj.ae2thing.api;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.network.CPacketNetworkCraftingItems;

import appeng.api.AEApi;
import appeng.api.storage.IItemDisplayRegistry;
import appeng.api.storage.data.IAEItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Pinned {

    private final HashMap<IAEItemStack, PinInfo> pinInfo = new HashMap<>();
    public static int MAX_PINNED = 9;
    public static Pinned INSTANCE = new Pinned();
    private long lastRunTime;
    private static final int interval = 1000;
    private static final Comparator<Map.Entry<IAEItemStack, PinInfo>> TIME_COMPARATOR = Comparator
        .comparing(e -> e.getValue().since);
    private static final IItemDisplayRegistry registry = AEApi.instance()
        .registries()
        .itemDisplay();

    public Set<IAEItemStack> getPinnedItems() {
        return pinInfo.keySet();
    }

    public boolean isEmpty() {
        return pinInfo.isEmpty();
    }

    public void add(IAEItemStack item) {
        if (item == null) return;
        if (registry.isBlacklisted(item.getItem()) || registry.isBlacklisted(
            item.getItem()
                .getClass())) {
            return;
        }
        PinInfo info = pinInfo.get(item);
        if (info != null) {
            info.since = Instant.now();
            info.canPrune = false;
        } else {
            pinInfo.put(item, new PinInfo(PinReason.CRAFTING));
        }

        if (pinInfo.size() > MAX_PINNED) {
            List<Map.Entry<IAEItemStack, PinInfo>> toRemove = new ArrayList<>(pinInfo.entrySet());
            toRemove.sort(TIME_COMPARATOR);
            for (Map.Entry<IAEItemStack, PinInfo> entry : toRemove.subList(0, toRemove.size() - MAX_PINNED)) {
                pinInfo.remove(entry.getKey());
            }
        }
    }

    public PinInfo remove(IAEItemStack item) {
        if (item == null) return null;
        return this.pinInfo.remove(item);
    }

    public boolean isPinnedItem(IAEItemStack item) {
        if (item == null) return false;
        return pinInfo.containsKey(item);
    }

    public List<IAEItemStack> getSortedPinnedItems() {
        List<Map.Entry<IAEItemStack, PinInfo>> list = new ArrayList<>(pinInfo.entrySet());
        list.sort(TIME_COMPARATOR);
        return list.stream()
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    @Nullable
    public PinInfo getPinInfo(IAEItemStack item) {
        if (item == null) return null;
        return this.pinInfo.get(item);
    }

    public void togglePinnedItems(IAEItemStack stack) {
        if (stack == null || this.remove(stack) != null) return;
        this.add(stack);
    }

    public int getMaxPinSize() {
        return MAX_PINNED;
    }

    public void updatePinnedItems(List<IAEItemStack> items) {
        GuiScreen gui = Minecraft.getMinecraft().currentScreen;
        if (!AE2ThingAPI.instance()
            .terminal()
            .isTerminal(gui)) return;
        if (items == null || items.isEmpty()) {
            pinInfo.values()
                .forEach(i -> i.canPrune = true);
            return;
        }
        HashSet<IAEItemStack> set = new HashSet<>(items);
        for (IAEItemStack item : this.pinInfo.keySet()) {
            if (!set.contains(item)) {
                this.pinInfo.get(item).canPrune = true;
            }
        }
    }

    public void prune() {
        pinInfo.values()
            .removeIf(v -> v.canPrune);
    }

    public void clear() {
        pinInfo.clear();
    }

    public void updateCraftingItems(boolean force) {
        GuiScreen gui = Minecraft.getMinecraft().currentScreen;
        if (gui == null) return;
        if (!AE2ThingAPI.instance()
            .terminal()
            .isTerminal(gui)) return;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastRunTime >= interval || force) {
            CPacketNetworkCraftingItems p = new CPacketNetworkCraftingItems();
            AE2Thing.proxy.netHandler.sendToServer(p);
            lastRunTime = currentTime;
        }
    }

    public void updateCraftingItems() {
        updateCraftingItems(false);
    }

    public static class PinInfo {

        public Instant since;
        public PinReason reason;
        public boolean canPrune;

        public PinInfo(PinReason reason) {
            this.reason = reason;
            this.since = Instant.now();
        }
    }

    public enum PinReason {
        CRAFTING
    }

}
