package com.asdflj.ae2thing.common.item;

import net.minecraft.item.ItemStack;

import appeng.items.AEBaseItem;

public abstract class BaseItem extends AEBaseItem {

    public ItemStack stack(int size, int meta) {
        return new ItemStack(this, size, meta);
    }

    public ItemStack stack(int size) {
        return new ItemStack(this, size);
    }

    public ItemStack stack() {
        return new ItemStack(this, 1);
    }
}
