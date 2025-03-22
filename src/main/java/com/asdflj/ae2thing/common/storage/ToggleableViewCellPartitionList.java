package com.asdflj.ae2thing.common.storage;

import java.util.Collections;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.asdflj.ae2thing.api.Constants;

import appeng.api.storage.data.IAEStack;
import appeng.util.Platform;
import appeng.util.prioitylist.IPartitionList;

public class ToggleableViewCellPartitionList<T extends IAEStack<T>> implements IPartitionList<T> {

    private final NBTTagCompound data;

    public ToggleableViewCellPartitionList(ItemStack viewCell) {
        this.data = Platform.openNbtData(viewCell);
    }

    @Override
    public boolean isListed(T input) {
        return data.getBoolean(Constants.VIEW_CELL);
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public Iterable getItems() {
        return Collections.EMPTY_LIST;
    }
}
