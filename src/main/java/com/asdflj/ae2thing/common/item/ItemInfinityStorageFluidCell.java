package com.asdflj.ae2thing.common.item;

import java.text.NumberFormat;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.common.storage.FluidCellInventoryHandler;
import com.asdflj.ae2thing.common.storage.ITFluidCellInventory;
import com.asdflj.ae2thing.common.storage.ITFluidCellInventoryHandler;
import com.asdflj.ae2thing.common.storage.infinityCell.InfinityFluidStorageCellInventory;
import com.asdflj.ae2thing.common.tabs.AE2ThingTabs;
import com.asdflj.ae2thing.loader.IRegister;
import com.asdflj.ae2thing.util.NameConst;
import com.glodblock.github.api.FluidCraftAPI;
import com.glodblock.github.common.storage.IStorageFluidCell;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.exceptions.AppEngException;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.util.Platform;
import cpw.mods.fml.common.registry.GameRegistry;

public class ItemInfinityStorageFluidCell extends BaseCellItem
    implements IStorageFluidCell, IRegister<ItemInfinityStorageFluidCell>, IItemInventoryHandler {

    private final int perType = 1;
    private final double idleDrain = 2000D;

    public ItemInfinityStorageFluidCell() {
        this.setMaxStackSize(1);
        this.setUnlocalizedName(NameConst.ITEM_INFINITY_FLUID_CELL);
        this.setTextureName(
            AE2Thing.resource(NameConst.ITEM_INFINITY_FLUID_CELL)
                .toString());
        this.setFeature(EnumSet.of(AEFeature.StorageCells));
    }

    @Override
    public ItemInfinityStorageFluidCell register() {
        GameRegistry.registerItem(this, NameConst.ITEM_INFINITY_FLUID_CELL, AE2Thing.MODID);
        setCreativeTab(AE2ThingTabs.INSTANCE);
        return this;
    }

    @Override
    public long getBytes(ItemStack cellItem) {
        return Long.MAX_VALUE;
    }

    @Override
    public int getBytesPerType(ItemStack cellItem) {
        return this.perType;
    }

    @Override
    public boolean isBlackListed(ItemStack cellItem, IAEFluidStack requestedAddition) {
        return requestedAddition == null || requestedAddition.getFluid() == null
            || FluidCraftAPI.instance()
                .isBlacklistedInStorage(
                    requestedAddition.getFluid()
                        .getClass());
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
    public double getIdleDrain(ItemStack is) {
        return this.idleDrain;
    }

    @Override
    public int getTotalTypes(ItemStack cellItem) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isEditable(ItemStack is) {
        return true;
    }

    @Override
    public IInventory getUpgradesInventory(ItemStack is) {
        return new CellUpgrades(is, 0);
    }

    @Override
    public IInventory getConfigInventory(ItemStack is) {
        return new CellConfig(is);
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack is) {
        final String fz = Platform.openNbtData(is)
            .getString("FuzzyMode");
        try {
            return FuzzyMode.valueOf(fz);
        } catch (final Throwable t) {
            return FuzzyMode.IGNORE_ALL;
        }
    }

    @Override
    public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {
        Platform.openNbtData(is)
            .setString("FuzzyMode", fzMode.name());
    }

    @Override
    public void addCheckedInformation(final ItemStack stack, final EntityPlayer player, final List<String> lines,
                                      final boolean displayMoreInfo) {
        final IMEInventoryHandler<?> inventory = AEApi.instance().registries().cell()
            .getCellInventory(stack, null, StorageChannel.FLUIDS);

        if (inventory instanceof final ITFluidCellInventoryHandler handler) {
            final ITFluidCellInventory cellInventory = handler.getCellInv();
            if (cellInventory != null) {
                lines.add(
                    EnumChatFormatting.WHITE + NumberFormat.getInstance().format(cellInventory.getStoredFluidTypes())
                        + EnumChatFormatting.GRAY
                        + " "
                        + GuiText.Of.getLocal()
                        + " "
                        + EnumChatFormatting.DARK_GREEN
                        + cellInventory.getTotalFluidTypes()
                        + " "
                        + EnumChatFormatting.GRAY
                        + GuiText.Types.getLocal());
                String uid = cellInventory.getUUID();
                if (!uid.isEmpty()) lines.add(uid);
                if (handler.isPreformatted()) {
                    final String list = (handler.getIncludeExcludeMode() == IncludeExclude.WHITELIST ? GuiText.Included
                        : GuiText.Excluded).getLocal();
                    lines.add(GuiText.Partitioned.getLocal() + " - " + list + ' ' + GuiText.Precise.getLocal());

                    if (GuiScreen.isShiftKeyDown()) {
                        lines.add(GuiText.Filter.getLocal() + ": ");
                        for (IAEFluidStack aeFluidStack : handler.getPartitionInv()) {
                            if (aeFluidStack != null) lines.add("  " + aeFluidStack.getFluidStack().getLocalizedName());
                        }
                    }
                }
            }
        }
        super.addCheckedInformation(stack, player, lines, displayMoreInfo);
    }

    @Override
    public StorageChannel getChannel() {
        return StorageChannel.FLUIDS;
    }

    @Override
    public IMEInventoryHandler<IAEFluidStack> getInventoryHandler(ItemStack o, ISaveProvider container,
        EntityPlayer player) throws AppEngException {
        return new FluidCellInventoryHandler(new InfinityFluidStorageCellInventory(o, container, player));
    }
}
