package com.asdflj.ae2thing.common.storage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.client.texture.ExtraBlockTextures;

public class CellHandler implements ICellHandler {

    @Override
    public boolean isCell(ItemStack is) {
        return false;
    }

    @Override
    public IMEInventoryHandler getCellInventory(ItemStack is, ISaveProvider container, StorageChannel channel) {
        if (channel == StorageChannel.ITEMS) {
            return CellInventory.getCell(is, container, null);
        }
        return null;
    }

    @Override
    public IIcon getTopTexture_Light() {
        return ExtraBlockTextures.BlockMEChestItems_Light.getIcon();
    }

    @Override
    public IIcon getTopTexture_Medium() {
        return ExtraBlockTextures.BlockMEChestItems_Medium.getIcon();
    }

    @Override
    public IIcon getTopTexture_Dark() {
        return ExtraBlockTextures.BlockMEChestItems_Dark.getIcon();
    }

    @Override
    public void openChestGui(EntityPlayer player, IChestOrDrive chest, ICellHandler cellHandler,
        IMEInventoryHandler inv, ItemStack is, StorageChannel chan) {}

    @Override
    public int getStatusForCell(final ItemStack is, final IMEInventory handler) {
        if (handler instanceof final CellInventoryHandler ci) {
            return ci.getStatusForCell();
        }
        return 0;
    }

    @Override
    public double cellIdleDrain(final ItemStack is, final IMEInventory handler) {
        final ITCellInventory inv = ((CellInventoryHandler) handler).getCellInv();
        return inv.getIdleDrain(is);
    }
}
