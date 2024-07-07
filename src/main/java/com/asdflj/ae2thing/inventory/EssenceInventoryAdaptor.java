package com.asdflj.ae2thing.inventory;

import java.util.Iterator;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import org.jetbrains.annotations.NotNull;

import com.asdflj.ae2thing.common.item.ItemPhial;
import com.asdflj.ae2thing.common.tile.TileInfusionInterface;

import appeng.api.config.FuzzyMode;
import appeng.util.InventoryAdaptor;
import appeng.util.inv.IInventoryDestination;
import appeng.util.inv.ItemSlot;

public class EssenceInventoryAdaptor extends InventoryAdaptor {

    private final InventoryAdaptor adaptor;
    private final TileInfusionInterface tile;

    public EssenceInventoryAdaptor(InventoryAdaptor ad, TileInfusionInterface tile) {
        this.adaptor = ad;
        this.tile = tile;

    }

    public static InventoryAdaptor getAdaptor(TileEntity tile, final ForgeDirection d) {
        InventoryAdaptor ad = InventoryAdaptor.getAdaptor(tile, d);
        if (ad == null) return null;
        TileEntity inter = tile.getWorldObj()
            .getTileEntity(tile.xCoord + d.offsetX, tile.yCoord + d.offsetY, tile.zCoord + d.offsetZ);
        if (inter instanceof TileInfusionInterface t) {
            return new EssenceInventoryAdaptor(ad, t);
        }
        return ad;
    }

    @Override
    public ItemStack removeItems(int amount, ItemStack filter, IInventoryDestination destination) {
        return adaptor.removeItems(amount, filter, destination);
    }

    @Override
    public ItemStack simulateRemove(int amount, ItemStack filter, IInventoryDestination destination) {
        return adaptor.simulateRemove(amount, filter, destination);
    }

    @Override
    public ItemStack removeSimilarItems(int amount, ItemStack filter, FuzzyMode fuzzyMode,
        IInventoryDestination destination) {
        return adaptor.removeSimilarItems(amount, filter, fuzzyMode, destination);
    }

    @Override
    public ItemStack simulateSimilarRemove(int amount, ItemStack filter, FuzzyMode fuzzyMode,
        IInventoryDestination destination) {
        return adaptor.simulateSimilarRemove(amount, filter, fuzzyMode, destination);
    }

    @Override
    public ItemStack addItems(ItemStack toBeAdded) {
        if (toBeAdded.getItem() instanceof ItemPhial) {
            return this.tile.addAspects(toBeAdded);
        } else {
            return adaptor.addItems(toBeAdded);
        }
    }

    @Override
    public ItemStack simulateAdd(ItemStack toBeSimulated) {
        return adaptor.simulateAdd(toBeSimulated);
    }

    @Override
    public boolean containsItems() {
        return adaptor.containsItems();
    }

    @NotNull
    @Override
    public Iterator<ItemSlot> iterator() {
        return adaptor.iterator();
    }
}
