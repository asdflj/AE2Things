package com.asdflj.ae2thing.api;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import com.asdflj.ae2thing.common.storage.StorageManager;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.me.Grid;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SuppressWarnings("unused")
public interface IAE2ThingAPI {

    boolean isBlacklistedInStorage(Item item);

    void blacklistItemInStorage(Class<? extends Item> item);

    void addBackpackItem(Class<? extends Item> item);

    boolean isBackpackItem(Item item);

    boolean isBackpackItem(ItemStack is);

    IInventory getBackpackInv(ItemStack is);

    boolean isBackpackItemInv(ItemStack is);

    StorageManager getStorageManager();

    void setStorageManager(StorageManager manager);

    Pinned getPinned();

    void openBackpackTerminal();

    @SideOnly(Side.CLIENT)
    void openDualinterfaceTerminal();

    ItemStack getFluidContainer(IAEFluidStack fluid);

    ItemStack getFluidContainer(FluidStack fluid);

    void setDefaultFluidContainer(ItemStack item);

    ItemStack getDefaultFluidContainer();

    String getVersion();

    Fluid getMana();

    void findCellItem(ItemStack item);

    long getStorageMyID(Grid grid);

    LimitedSizeLinkedList<CraftingDebugHelper.CraftingInfo> getHistory(Grid grid);

    LimitedSizeLinkedList<CraftingDebugHelper.CraftingInfo> getHistory(long networkID);

    void pushHistory(long networkID, LimitedSizeLinkedList<CraftingDebugHelper.CraftingInfo> infos);

    void saveHistory();

    void openTerminalMenu();

    @SideOnly(Side.CLIENT)
    void addCraftingCompleteNotification(IAEItemStack item);

    @SideOnly(Side.CLIENT)
    void addNotification(String tile, String Content, ItemStack item);
}
