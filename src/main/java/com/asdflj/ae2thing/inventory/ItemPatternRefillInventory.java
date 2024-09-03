package com.asdflj.ae2thing.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.api.AEApi;
import appeng.core.AELog;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;

public class ItemPatternRefillInventory extends AppEngInternalInventory {

    private final ItemStack is;
    private final String name;
    private final EntityPlayer player;
    private final int slot;

    public ItemPatternRefillInventory(ItemStack is, String name, int size, int maxStack, EntityPlayer player,
        int slot) {
        super(null, size, maxStack);
        this.name = name;
        this.is = is;
        this.player = player;
        this.slot = slot;
        this.readFromNBT(Platform.openNbtData(this.is), this.name);
    }

    @Override
    protected void writeToNBT(final NBTTagCompound target) {
        for (int x = 0; x < this.getSizeInventory(); x++) {
            try {
                final NBTTagCompound c = new NBTTagCompound();

                if (this.inv[x] != null) {
                    Platform.writeItemStackToNBT(this.inv[x], c);
                }

                target.setTag("#" + x, c);
            } catch (final Exception ignored) {}
        }
    }

    @Override
    public void readFromNBT(final NBTTagCompound target) {
        for (int x = 0; x < this.getSizeInventory(); x++) {
            try {
                final NBTTagCompound c = target.getCompoundTag("#" + x);

                if (c != null) {
                    this.inv[x] = Platform.loadItemStackFromNBT(c);
                }
            } catch (final Exception e) {
                AELog.debug(e);
            }
        }
    }

    @Override
    public void markDirty() {
        this.writeToNBT(Platform.openNbtData(is), this.name);
        if (Platform.isServer()) this.player.inventory.setInventorySlotContents(slot, this.is);
    }

    @Override
    public boolean isItemValidForSlot(final int i, final ItemStack itemstack) {
        return i == 0 && getStackInSlot(0) == null
            && AEApi.instance()
                .definitions()
                .materials()
                .cardPatternRefiller()
                .isSameAs(itemstack);
    }
}
