package com.asdflj.ae2thing.api;

import java.util.List;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.common.storage.StorageManager;

import appeng.api.storage.data.IAEItemStack;

@SuppressWarnings("unused")
public interface IAE2ThingAPI {

    boolean isBlacklistedInStorage(Item item);

    void blacklistItemInStorage(Class<? extends Item> item);

    void addBackpackItem(Class<? extends Item> item);

    boolean isBackpackItem(Item item);

    boolean isBackpackItem(ItemStack is);

    IInventory getBackpackInv(ItemStack is);

    boolean isBackpackItemInv(ItemStack is);

    StorageManager getStorageManager();

    void setStorageManager(StorageManager manager);

    List<IAEItemStack> getPinItems();

    void setPinItems(List<IAEItemStack> items);

    void togglePinItems(IAEItemStack stack);

    void openBackpackTerminal();
}
