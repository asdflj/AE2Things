package com.asdflj.ae2thing.common.item;

import java.text.NumberFormat;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.common.storage.CellInventoryHandler;
import com.asdflj.ae2thing.common.storage.ICellInventoryHandler;
import com.asdflj.ae2thing.common.storage.ITCellInventory;
import com.asdflj.ae2thing.common.storage.infinityCell.InfinityItemStorageCellInventory;
import com.asdflj.ae2thing.common.tabs.AE2ThingTabs;
import com.asdflj.ae2thing.loader.IRegister;
import com.asdflj.ae2thing.util.NameConst;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.exceptions.AppEngException;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.util.Platform;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemInfinityStorageCell extends BaseCellItem
    implements IStorageCell, IItemGroup, IRegister<ItemInfinityStorageCell>, IItemInventoryHandler {

    private final int perType = 1;
    private final double idleDrain = 2000D;

    @SuppressWarnings("Guava")
    public ItemInfinityStorageCell() {
        this.setMaxStackSize(1);
        this.setUnlocalizedName(NameConst.ITEM_INFINITY_CELL);
        this.setTextureName(
            AE2Thing.resource(NameConst.ITEM_INFINITY_CELL)
                .toString());
        this.setFeature(EnumSet.of(AEFeature.StorageCells));
    }

    @Override
    public String getUnlocalizedGroupName(Set<ItemStack> otherItems, ItemStack is) {
        return GuiText.StorageCells.getUnlocalized();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addCheckedInformation(final ItemStack stack, final EntityPlayer player, final List<String> lines,
        final boolean displayMoreInfo) {
        final IMEInventoryHandler<?> inventory = AEApi.instance()
            .registries()
            .cell()
            .getCellInventory(stack, null, StorageChannel.ITEMS);

        if (inventory instanceof ICellInventoryHandler handler) {
            final ITCellInventory cellInventory = handler.getCellInv();

            if (cellInventory != null) {
                lines.add(
                    EnumChatFormatting.WHITE + NumberFormat.getInstance()
                        .format(cellInventory.getStoredItemTypes())
                        + EnumChatFormatting.GRAY
                        + " "
                        + GuiText.Of.getLocal()
                        + " "
                        + EnumChatFormatting.DARK_GREEN
                        + NumberFormat.getInstance()
                            .format(cellInventory.getTotalItemTypes())
                        + " "
                        + EnumChatFormatting.GRAY
                        + GuiText.Types.getLocal());
                String uid = cellInventory.getUUID();
                if (!uid.isEmpty()) lines.add(uid);
                if (handler.isPreformatted()) {
                    String filter = cellInventory.getOreFilter();
                    if (filter.isEmpty()) {
                        final String list = (handler.getIncludeExcludeMode() == IncludeExclude.WHITELIST
                            ? GuiText.Included
                            : GuiText.Excluded).getLocal();

                        if (handler.isFuzzy()) {
                            lines.add(GuiText.Partitioned.getLocal() + " - " + list + ' ' + GuiText.Fuzzy.getLocal());
                        } else {
                            lines.add(GuiText.Partitioned.getLocal() + " - " + list + ' ' + GuiText.Precise.getLocal());
                        }
                        if (GuiScreen.isShiftKeyDown()) {
                            lines.add(GuiText.Filter.getLocal() + ": ");
                            for (int i = 0; i < cellInventory.getConfigInventory()
                                .getSizeInventory(); ++i) {
                                ItemStack s = cellInventory.getConfigInventory()
                                    .getStackInSlot(i);
                                if (s != null) lines.add(s.getDisplayName());
                            }
                        }
                    } else {
                        lines.add(GuiText.PartitionedOre.getLocal() + " : " + filter);
                    }

                    if (handler.getSticky()) {
                        lines.add(GuiText.Sticky.getLocal());
                    }
                }
            }
        }
        super.addCheckedInformation(stack, player, lines, displayMoreInfo);
    }

    @Override
    public int getBytes(ItemStack cellItem) {
        return Integer.MAX_VALUE;
    }

    @Override
    public long getBytesLong(final ItemStack cellItem) {
        return Long.MAX_VALUE;
    }

    @Override
    public int BytePerType(ItemStack cellItem) {
        return this.perType;
    }

    @Override
    public int getBytesPerType(ItemStack cellItem) {
        return this.perType;
    }

    @Override
    public int getTotalTypes(ItemStack cellItem) {
        return Integer.MAX_VALUE;
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
        return this.idleDrain;
    }

    @Override
    public boolean isEditable(ItemStack is) {
        return true;
    }

    @Override
    public IInventory getUpgradesInventory(ItemStack is) {
        return new CellUpgrades(is, 2);
    }

    @Override
    public IInventory getConfigInventory(ItemStack is) {
        return new CellConfig(is);
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack is) {
        return FuzzyMode.fromItemStack(is);
    }

    @Override
    public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {
        Platform.openNbtData(is)
            .setString("FuzzyMode", fzMode.name());
    }

    @Override
    public ItemInfinityStorageCell register() {
        GameRegistry.registerItem(this, NameConst.ITEM_INFINITY_CELL, AE2Thing.MODID);
        setCreativeTab(AE2ThingTabs.INSTANCE);
        return this;
    }

    @Override
    public StorageChannel getChannel() {
        return StorageChannel.ITEMS;
    }

    @Override
    public IMEInventoryHandler<IAEItemStack> getInventoryHandler(ItemStack o, ISaveProvider container,
        EntityPlayer player) throws AppEngException {
        return new CellInventoryHandler(new InfinityItemStorageCellInventory(o, container, player));
    }
}
