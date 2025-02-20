package com.asdflj.ae2thing.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import com.asdflj.ae2thing.api.adapter.findit.IFindItAdapter;
import com.asdflj.ae2thing.api.adapter.terminal.ICraftingTerminalAdapter;
import com.asdflj.ae2thing.common.storage.StorageManager;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.client.gui.AEBaseGui;
import appeng.me.Grid;

@SuppressWarnings("unused")
public interface IAE2ThingAPI {

    boolean isBlacklistedInStorage(Item item);

    void registerTerminal(Class<? extends AEBaseGui> clazz);

    HashSet<Class<? extends AEBaseGui>> getTerminal();

    void registerTerminalBlackList(Class<? extends AEBaseGui> clazz);

    boolean isTerminal(GuiScreen gui);

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

    void registerCraftingTerminal(ICraftingTerminalAdapter adapter);

    HashMap<Class<? extends Container>, ICraftingTerminalAdapter> getCraftingTerminal();

    boolean isCraftingTerminal(Class<? extends Container> terminal);

    void registerFindItStorageProvider(IFindItAdapter adapter);

    Collection<IFindItAdapter> getStorageProviders();

    boolean isCraftingTerminal(GuiScreen terminal);

    void addTrackingMissingItem(IAEItemStack is);

    IItemList<IAEItemStack> getTrakingMissingItems();

    void clearTrakingMissingItems();
}
