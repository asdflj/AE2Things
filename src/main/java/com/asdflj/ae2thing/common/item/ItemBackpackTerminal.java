package com.asdflj.ae2thing.common.item;

import static net.minecraft.client.gui.GuiScreen.isShiftKeyDown;

import java.util.List;

import net.minecraft.client.resources.I18n;
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
import cpw.mods.fml.common.registry.GameRegistry;

public class ItemBackpackTerminal extends BaseItem
    implements IRegister<ItemBackpackTerminal>, IItemInventory, IStorageItemCell {

    public ItemBackpackTerminal() {
        super();
        this.setMaxStackSize(1);
        this.setUnlocalizedName(NameConst.ITEM_BACKPACK_TERMINAL);
        this.setTextureName(
            AE2Thing.resource(NameConst.ITEM_BACKPACK_TERMINAL)
                .toString());
    }

    @Override
    public ItemBackpackTerminal register() {
        GameRegistry.registerItem(this, NameConst.ITEM_BACKPACK_TERMINAL, AE2Thing.MODID);
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
        return GuiType.BACKPACK_TERMINAL;
    }

    @Override
    public boolean isBlackListed(ItemStack cellItem, IAEItemStack requestedAddition) {
        return requestedAddition == null || requestedAddition.getItem() == null
            || AE2ThingAPI.instance()
                .isBlacklistedInStorage(requestedAddition.getItem());
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

    @Override
    protected void addCheckedInformation(ItemStack stack, EntityPlayer player, List<String> toolTip,
        boolean displayMoreInfo) {
        super.addCheckedInformation(stack, player, toolTip, displayMoreInfo);
        if (isShiftKeyDown()) {
            toolTip.add(I18n.format(NameConst.TT_BACKPACK_TERMINAL_DESC));
        } else {
            toolTip.add(I18n.format(NameConst.TT_SHIFT_FOR_MORE));
        }
    }
}
