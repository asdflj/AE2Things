package com.asdflj.ae2thing.common.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.common.storage.IStorageItemCell;
import com.asdflj.ae2thing.common.tabs.AE2ThingTabs;
import com.asdflj.ae2thing.inventory.InventoryHandler;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.asdflj.ae2thing.inventory.item.IItemInventory;
import com.asdflj.ae2thing.inventory.item.PortableItemInventory;
import com.asdflj.ae2thing.loader.IRegister;
import com.asdflj.ae2thing.util.BlockPos;
import com.asdflj.ae2thing.util.NameConst;
import com.asdflj.ae2thing.util.Util;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEItemStack;
import appeng.items.AEBaseItem;
import cpw.mods.fml.common.registry.GameRegistry;

public class ItemBackpackManager extends AEBaseItem
    implements IRegister<ItemBackpackManager>, IItemInventory, IStorageItemCell {

    public ItemBackpackManager() {
        super();
        this.setMaxStackSize(1);
        this.setUnlocalizedName(NameConst.ITEM_BACKPACK_MANAGER);
        this.setTextureName(
            AE2Thing.resource(NameConst.ITEM_BACKPACK_MANAGER)
                .toString());
    }

    @Override
    public ItemBackpackManager register() {
        GameRegistry.registerItem(this, NameConst.ITEM_BACKPACK_MANAGER, AE2Thing.MODID);
        setCreativeTab(AE2ThingTabs.INSTANCE);
        return this;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack item, World w, EntityPlayer player) {
        InventoryHandler.openGui(
            player,
            w,
            new BlockPos(player.inventory.currentItem, 0, 0),
            ForgeDirection.UNKNOWN,
            this.guiGuiType(item));
        return item;
    }

    @Override
    public Object getInventory(ItemStack stack, World world, int x, int y, int z, EntityPlayer player) {
        return new PortableItemInventory(stack, Util.findItemStack(player, stack), player);
    }

    private GuiType guiGuiType(ItemStack item) {
        return GuiType.BACKPACK_MANAGER;
    }

    public ItemStack stack(int size, int meta) {
        return new ItemStack(this, size, meta);
    }

    public ItemStack stack(int size) {
        return new ItemStack(this, size);
    }

    public ItemStack stack() {
        return new ItemStack(this, 1);
    }

    @Override
    public boolean isBlackListed(ItemStack cellItem, IAEItemStack requestedAddition) {
        return requestedAddition == null || requestedAddition.getItem() == null
            || AE2ThingAPI.instance()
                .isBlacklistedInStorage(
                    requestedAddition.getItem()
                        .getClass());
    }

    @Override
    public boolean isEditable(ItemStack is) {
        return false;
    }

    @Override
    public IInventory getUpgradesInventory(ItemStack is) {
        return null;
    }

    @Override
    public IInventory getConfigInventory(ItemStack is) {
        return null;
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack is) {
        return null;
    }

    @Override
    public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {

    }
}
