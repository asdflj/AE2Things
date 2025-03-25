package com.asdflj.ae2thing.inventory.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.inventory.ItemBiggerAppEngInventory;

import appeng.api.AEApi;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.IConfigManager;
import appeng.api.util.IInterfaceViewable;
import appeng.container.interfaces.IInventorySlotAware;
import appeng.util.Platform;

public class PatternModifierInventory implements ITerminalHost, IInventorySlotAware, IGuiItemObject {

    private final int slot;
    private final ItemStack item;
    private final EntityPlayer player;
    protected final ItemBiggerAppEngInventory pattern;
    protected final ItemBiggerAppEngInventory replace;

    public PatternModifierInventory(ItemStack item, int slot, EntityPlayer player) {
        this.slot = slot;
        this.item = item;
        this.player = player;
        this.pattern = new ItemBiggerAppEngInventory(this.item, Constants.PATTERN, 9 * 4, this.player, slot, null, 1);
        this.replace = new ItemBiggerAppEngInventory(this.item, Constants.REPLACE, 2, this.player, slot);
    }

    public PatternModifierInventory(ItemStack item, EntityPlayer player) {
        this(item, findPatternModifierSlot(item, player), player);
    }

    private static int findPatternModifierSlot(ItemStack item, EntityPlayer player) {
        for (int i = 0; i < player.inventory.mainInventory.length; i++) {
            ItemStack itemstack = player.inventory.mainInventory[i];
            if (Platform.isSameItemPrecise(itemstack, item)) {
                return i;
            }
        }
        throw new RuntimeException("Can't find pattern modifier slot");
    }

    @Override
    public IConfigManager getConfigManager() {
        return null;
    }

    @Override
    public ItemStack getItemStack() {
        return item;
    }

    @Override
    public IMEMonitor<IAEItemStack> getItemInventory() {
        return null;
    }

    @Override
    public IMEMonitor<IAEFluidStack> getFluidInventory() {
        return null;
    }

    @Override
    public int getInventorySlot() {
        return slot;
    }

    public IInventory getInventoryByName(String crafting) {
        if (crafting.equals(Constants.PATTERN)) {
            return this.pattern;
        } else if (crafting.equals(Constants.REPLACE)) {
            return this.replace;
        }
        return null;
    }

    public boolean injectItems(ItemStack itemStack) {
        for (int i = 0; i < this.pattern.getSizeInventory(); i++) {
            ItemStack stack = this.pattern.getStackInSlot(i);
            if (stack == null) {
                this.pattern.setInventorySlotContents(i, itemStack);
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public void extractToHost(IInterfaceViewable host) {
        if (host == null) return;
        IItemList<IAEItemStack> itemList = this.pattern.getMEInventory()
            .getAvailableItems(
                AEApi.instance()
                    .storage()
                    .createPrimitiveItemList());
        if (itemList.isEmpty()) return;
        IInventory inv = host.getPatterns();
        for (int i = 0; i < host.rowSize() * host.rows(); i++) {
            ItemStack stored = inv.getStackInSlot(i);
            if (stored != null) continue;
            for (int j = 0; j < this.pattern.getSizeInventory(); j++) {
                ItemStack stack = this.pattern.getStackInSlot(j);
                if (stack != null) {
                    inv.setInventorySlotContents(i, stack);
                    this.pattern.setInventorySlotContents(j, null);
                    break;
                }
            }
        }
    }

}
