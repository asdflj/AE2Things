package com.asdflj.ae2thing.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.common.storage.StorageManager;

import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.container.interfaces.IInventorySlotAware;
import appeng.tile.inventory.BiggerAppEngInventory;
import appeng.util.Platform;

public class ItemDiskCloneInventory extends BiggerAppEngInventory implements IGuiItemObject, IInventorySlotAware {

    private final ItemStack is;
    private final String name;
    private final EntityPlayer player;
    private final int slot;

    public ItemDiskCloneInventory(ItemStack is, String name, EntityPlayer player, int slot) {
        super(null, 1);
        this.name = name;
        this.is = is;
        this.player = player;
        this.slot = slot;
        this.readFromNBT(Platform.openNbtData(is), name);
    }

    @Override
    public void markDirty() {
        this.writeToNBT(Platform.openNbtData(is), this.name);
        if (Platform.isServer()) this.player.inventory.setInventorySlotContents(slot, this.is);
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack disk) {
        if (Platform.isServer() && disk != null
            && disk.getItem() != null
            && is.getItem() != null
            && disk.getItem()
                .equals(is.getItem())) {
            StorageManager m = AE2ThingAPI.instance()
                .getStorageManager();
            String uid = m.getStorage(this.is, this.player)
                .getUUID();
            m.setStorage(uid, disk);
        }
        super.setInventorySlotContents(slot, disk);
    }

    @Override
    public ItemStack getItemStack() {
        return this.is;
    }

    @Override
    public int getInventorySlot() {
        return this.slot;
    }
}
