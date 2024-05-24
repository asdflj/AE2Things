package com.asdflj.ae2thing.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.common.storage.StorageManager;

import appeng.api.storage.data.IAEItemStack;

public final class AE2ThingAPI implements IAE2ThingAPI {

    private static final AE2ThingAPI API = new AE2ThingAPI();

    private final Set<Class<? extends Item>> backpackItems = new HashSet<>();
    private StorageManager storageManager = null;
    private final List<IAEItemStack> pinItems = new ArrayList<>();
    public static int maxPinSize = 9;

    public static AE2ThingAPI instance() {
        return API;
    }

    @Override
    public boolean isBlacklistedInStorage(Item item) {
        if (item instanceof IBackpackItem) return true;
        for (Class<? extends Item> cls : backpackItems) {
            if (cls.isInstance(item)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void blacklistItemInStorage(Class<? extends Item> item) {
        backpackItems.add(item);
    }

    @Override
    public void addBackpackItem(Class<? extends Item> item) {
        blacklistItemInStorage(item);
    }

    @Override
    public boolean isBackpackItem(Item item) {
        return isBlacklistedInStorage(item);
    }

    @Override
    public boolean isBackpackItem(ItemStack itemStack) {
        return itemStack != null && itemStack.getItem() != null && isBackpackItem(itemStack.getItem());
    }

    @Override
    public IInventory getBackpackInv(ItemStack is) {
        if (is == null || is.getItem() == null) return null;
        if (is.getItem() instanceof IBackpackItem ibi) {
            return ibi.getInventory(is);
        }
        return null;
    }

    @Override
    public boolean isBackpackItemInv(ItemStack is) {
        if (is == null || is.getItem() == null) return false;
        return is.getItem() instanceof IBackpackItem;
    }

    @Override
    public StorageManager getStorageManager() {
        return storageManager;
    }

    @Override
    public void setStorageManager(StorageManager manager) {
        storageManager = manager;
    }

    @Override
    public List<IAEItemStack> getPinItems() {
        return this.pinItems;
    }

    @Override
    public void setPinItems(List<IAEItemStack> items) {
        this.pinItems.clear();
        this.pinItems.addAll(items);
    }

    @Override
    public void togglePinItems(IAEItemStack stack) {
        if (stack == null || this.pinItems.remove(stack)) return;
        this.pinItems.add(stack);
        if (this.pinItems.size() > maxPinSize) {
            List<IAEItemStack> tmp = new ArrayList<>(
                this.pinItems.subList(this.pinItems.size() - maxPinSize, this.pinItems.size()));
            this.pinItems.clear();
            this.pinItems.addAll(tmp);
        }
    }

}
