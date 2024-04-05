package com.asdflj.ae2thing.common.storage;

import net.minecraft.item.ItemStack;

import appeng.api.storage.ICellWorkbenchItem;
import appeng.api.storage.data.IAEItemStack;

public interface IStorageItemCell extends ICellWorkbenchItem {

    boolean isBlackListed(ItemStack cellItem, IAEItemStack requestedAddition);
}
