package com.asdflj.ae2thing.api;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public interface IBackpackItem {

    IInventory getInventory(ItemStack is);
}
