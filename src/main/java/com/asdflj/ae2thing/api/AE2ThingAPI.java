package com.asdflj.ae2thing.api;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public final class AE2ThingAPI implements IAE2ThingAPI {

    private static final AE2ThingAPI API = new AE2ThingAPI();

    private final Set<Class<? extends Item>> backpackItems = new HashSet<>();
    private final HashMap<Class<? extends Item>, Class<? extends IInventory>> backpacks = new HashMap<>();

    public static AE2ThingAPI instance() {
        return API;
    }

    @Override
    public boolean isBlacklistedInStorage(Class<? extends Item> item) {
        return backpackItems.contains(item);
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
    public void addBackpackItem(Class<? extends Item> item, Class<? extends IInventory> inv) {
        backpacks.put(item, inv);
        blacklistItemInStorage(item);

    }

    @Override
    public boolean isBackpackItem(Class<? extends Item> item) {
        return isBlacklistedInStorage(item);
    }

    @Override
    public boolean isBackpackItem(ItemStack itemStack) {
        return itemStack != null && itemStack.getItem() != null
            && isBackpackItem(
                itemStack.getItem()
                    .getClass());
    }

    @Override
    public IInventory getBackpackInv(ItemStack is) {
        if (is == null || is.getItem() == null) return null;
        Class<? extends IInventory> inv = backpacks.get(
            is.getItem()
                .getClass());
        if (inv == null) return null;
        try {
            return inv.getConstructor(ItemStack.class)
                .newInstance(is);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException
            | NoSuchMethodException ignored) {}
        return null;
    }

    @Override
    public boolean isBackpackItemInv(ItemStack is) {
        if (is == null || is.getItem() == null) return false;
        return backpacks.get(
            is.getItem()
                .getClass())
            != null;
    }

}
