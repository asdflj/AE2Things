package com.asdflj.ae2thing.common.storage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;

import com.asdflj.ae2thing.common.item.IItemInventoryHandler;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.util.BlockPos;

import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.client.texture.ExtraBlockTextures;
import appeng.core.sync.GuiBridge;
import appeng.util.Platform;

public class CellHandler implements ICellHandler {

    @Override
    public boolean isCell(ItemStack is) {
        return is != null && is.getItem() instanceof IItemInventoryHandler;
    }

    @Override
    public IMEInventoryHandler getCellInventory(ItemStack is, ISaveProvider container, StorageChannel channel) {
        try {
            if (is.getItem() instanceof IItemInventoryHandler iih) {
                if (iih.getChannel() == channel) {
                    return iih.getInventoryHandler(is, container, null);
                }
            }
        } catch (Exception ignored) {}
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
        IMEInventoryHandler inv, ItemStack is, StorageChannel chan) {
        if (chest instanceof TileEntity te) {
            if (chan == StorageChannel.FLUIDS) {
                InventoryHandler
                    .openGui(player, te.getWorldObj(), new BlockPos(te), chest.getUp(), GuiType.FLUID_TERMINAL);
            } else {
                Platform.openGUI(player, te, chest.getUp(), GuiBridge.GUI_ME);
            }
        }
    }

    @Override
    public int getStatusForCell(final ItemStack is, final IMEInventory handler) {
        if (handler instanceof final CellInventoryHandler ci) {
            return ci.getStatusForCell();
        }else if (handler instanceof final FluidCellInventoryHandler ci) {
            return ci.getStatusForCell();
        }
        return 0;
    }

    @Override
    public double cellIdleDrain(final ItemStack is, final IMEInventory handler) {
        if (handler instanceof CellInventoryHandler ci) {
            return ci.getCellInv()
                .getIdleDrain(is);
        } else if (handler instanceof FluidCellInventoryHandler ci) {
            return ci.getCellInv()
                .getIdleDrain(is);
        }
        return 0;
    }
}
