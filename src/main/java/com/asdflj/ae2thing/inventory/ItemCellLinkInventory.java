package com.asdflj.ae2thing.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.common.storage.StorageManager;
import com.asdflj.ae2thing.common.storage.infinityCell.BaseInventory;
import com.asdflj.ae2thing.network.SPacketMEItemInvUpdate;

import appeng.container.interfaces.IInventorySlotAware;
import appeng.tile.inventory.BiggerAppEngInventory;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;

public class ItemCellLinkInventory extends BiggerAppEngInventory implements BaseInventory, IInventorySlotAware {

    private final ItemStack is;
    private final EntityPlayer player;
    private final int slot;

    public ItemCellLinkInventory(ItemStack is, EntityPlayer player, int slot) {
        super(null, 1);
        this.is = is;
        this.player = player;
        this.slot = slot;
        this.genUUID();
    }

    private void genUUID() {
        if (Platform.isServer() && Platform.openNbtData(is)
            .hasNoTags()) {
            StorageManager m = AE2ThingAPI.instance()
                .getStorageManager();
            m.getStorage(this.is, this.player);
            SPacketMEItemInvUpdate piu = new SPacketMEItemInvUpdate((byte) -1);
            piu.appendItem(AEItemStack.create(this.is));
            AE2Thing.proxy.netHandler.sendTo(piu, (EntityPlayerMP) player);
        }
    }

    @Override
    public void markDirty() {}

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

    @Override
    public String getUUID() {
        NBTTagCompound data = Platform.openNbtData(is);
        return data.getString(Constants.DISKUUID);
    }
}
