package com.asdflj.ae2thing.api;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@SuppressWarnings("unused")
public interface IAE2ThingAPI {

    boolean isBlacklistedInStorage(Class<? extends Item> item);

    void blacklistItemInStorage(Class<? extends Item> item);

    void addBackpackItem(Class<? extends Item> item);

    void addBackpackItem(Class<? extends Item> item, Class<? extends IInventory> inv);

    boolean isBackpackItem(Class<? extends Item> item);

    boolean isBackpackItem(ItemStack is);

    IInventory getBackpackInv(ItemStack is);

    boolean isBackpackItemInv(ItemStack is);
}
