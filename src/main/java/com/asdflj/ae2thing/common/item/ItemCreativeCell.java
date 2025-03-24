package com.asdflj.ae2thing.common.item;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.common.storage.CellInventoryHandler;
import com.asdflj.ae2thing.common.storage.infinityCell.CreativeCellInventory;
import com.asdflj.ae2thing.common.tabs.AE2ThingTabs;

import appeng.api.config.FuzzyMode;
import appeng.api.exceptions.AppEngException;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.items.contents.CellUpgrades;
import appeng.tile.inventory.AppEngInternalInventory;
import cpw.mods.fml.common.registry.GameRegistry;

public class ItemCreativeCell extends BaseCellItem implements IStorageCell, IItemGroup {

    public static ItemStack cobblestone = new ItemStack(Blocks.cobblestone, 1);
    private final int perType = 1;
    private final double idleDrain = 2000D;

    @Override
    public String getUnlocalizedGroupName(Set<ItemStack> otherItems, ItemStack is) {
        return GuiText.StorageCells.getUnlocalized();
    }

    protected static class InfinityConfig extends AppEngInternalInventory {

        public InfinityConfig(final ItemStack is) {
            super(null, 1);
            this.setInventorySlotContents(0, is);
        }

        @Override
        public void markDirty() {}
    }

    private final ItemStack is;
    private final String name;

    public ItemCreativeCell(String name, ItemStack is, String textureName) {
        this.is = is;
        this.name = name;
        setUnlocalizedName(this.name);
        setTextureName(
            AE2Thing.resource(textureName)
                .toString());
        this.setMaxStackSize(1);
        this.setFeature(EnumSet.of(AEFeature.StorageCells));
    }

    public ItemCreativeCell(String name, ItemStack is) {
        this(name, is, name);
    }

    @Override
    public IMEInventoryHandler<?> getInventoryHandler(ItemStack o, ISaveProvider container, EntityPlayer player)
        throws AppEngException {
        return new CellInventoryHandler(new CreativeCellInventory(o));
    }

    @Override
    public StorageChannel getChannel() {
        return StorageChannel.ITEMS;
    }

    @Override
    public ItemCreativeCell register() {
        GameRegistry.registerItem(this, this.name, AE2Thing.MODID);
        setCreativeTab(AE2ThingTabs.INSTANCE);
        return this;
    }

    @Override
    public int getBytes(ItemStack cellItem) {
        return Integer.MAX_VALUE;
    }

    @Override
    public int BytePerType(ItemStack cellItem) {
        return perType;
    }

    @Override
    public int getBytesPerType(ItemStack cellItem) {
        return perType;
    }

    @Override
    public int getTotalTypes(ItemStack cellItem) {
        return 1;
    }

    @Override
    public boolean isBlackListed(ItemStack cellItem, IAEItemStack requestedAddition) {
        return false;
    }

    @Override
    public boolean storableInStorageCell() {
        return false;
    }

    @Override
    public boolean isStorageCell(ItemStack i) {
        return false;
    }

    @Override
    public double getIdleDrain() {
        return idleDrain;
    }

    @Override
    public boolean isEditable(ItemStack is) {
        return false;
    }

    @Override
    public IInventory getUpgradesInventory(ItemStack is) {
        return new CellUpgrades(is, 2);
    }

    @Override
    public IInventory getConfigInventory(ItemStack is) {
        return new InfinityConfig(this.is);
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack is) {
        return null;
    }

    @Override
    public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {

    }

    @Override
    public void addCheckedInformation(final ItemStack stack, final EntityPlayer player, final List<String> lines,
        final boolean displayMoreInfo) {

    }
}
