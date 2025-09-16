package com.asdflj.ae2thing.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;

import com.asdflj.ae2thing.api.adapter.crafting.ICraftingTerminalAdapter;
import com.asdflj.ae2thing.api.adapter.findit.IFindItAdapter;
import com.asdflj.ae2thing.api.adapter.pattern.IPatternTerminalAdapter;
import com.asdflj.ae2thing.api.adapter.terminal.ITerminal;
import com.asdflj.ae2thing.api.adapter.terminal.item.ITerminalHandler;
import com.asdflj.ae2thing.client.gui.GuiCraftingTerminal;
import com.asdflj.ae2thing.client.gui.widget.IGuiMonitor;
import com.asdflj.ae2thing.nei.ButtonConstants;
import com.asdflj.ae2thing.nei.NEI_TH_Config;
import com.asdflj.ae2thing.util.Util;

import appeng.api.AEApi;
import appeng.api.networking.IGridHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.client.gui.AEBaseGui;
import codechicken.nei.recipe.GuiRecipe;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class Terminal {

    public static Terminal API = new Terminal();
    private static final HashSet<Class<? extends AEBaseGui>> terminal = new HashSet<>();
    private static final HashSet<Class<? extends AEBaseGui>> terminalBlackList = new HashSet<>();
    private static final HashMap<Class<? extends Container>, ICraftingTerminalAdapter> craftingTerminal = new HashMap<>();
    private static final IItemList<IAEItemStack> tracking = AEApi.instance()
        .storage()
        .createPrimitiveItemList();
    private static final HashMap<Class<? extends IGridHost>, IFindItAdapter> storageProviders = new HashMap<>();
    private static final HashMap<Class<? extends Container>, IPatternTerminalAdapter> patternTerminal = new HashMap<>();
    private static final HashMap<Class<? extends Item>, ITerminalHandler> terminalItem = new HashMap<>();
    private static final HashSet<ITerminal> terminalSet = new HashSet<>();

    public void registerTerminal(Class<? extends AEBaseGui> clazz) {
        terminal.add(clazz);
    }

    public HashSet<Class<? extends AEBaseGui>> getTerminal() {
        return terminal;
    }

    public HashSet<ITerminal> getTerminalSet() {
        return terminalSet;
    }

    public void registerTerminalSet(ITerminal iTerminal) {
        terminalSet.add(iTerminal);
    }

    public void registerTerminalBlackList(Class<? extends AEBaseGui> clazz) {
        terminalBlackList.add(clazz);
    }

    @SideOnly(Side.CLIENT)
    public boolean isTerminal(GuiScreen gui) {
        if (gui == null) return false;
        if (gui instanceof IGuiMonitor) {
            return true;
        }
        return terminal.contains(gui.getClass());
    }

    public boolean isBackPackTerminal(GuiScreen gui) {
        return gui instanceof GuiCraftingTerminal;
    }

    @SideOnly(Side.CLIENT)
    public boolean isPinTerminal(GuiScreen gui) {
        if (Util.getAEVersion() >= 586) {
            return false;
        }
        if (!NEI_TH_Config.getConfigValue(ButtonConstants.PINNED_BAR)) return false;
        if (gui == null || terminalBlackList.contains(gui.getClass())) {
            return false;
        }
        if (gui instanceof IGuiMonitor) {
            return true;
        }
        return terminal.contains(gui.getClass());
    }

    public void registerCraftingTerminal(ICraftingTerminalAdapter adapter) {
        craftingTerminal.put(adapter.getContainer(), adapter);
    }

    public HashMap<Class<? extends Container>, ICraftingTerminalAdapter> getCraftingTerminal() {
        return craftingTerminal;
    }

    public boolean isCraftingTerminal(Class<? extends Container> terminal) {
        return craftingTerminal.containsKey(terminal);
    }

    public void registerFindItStorageProvider(IFindItAdapter adapter) {
        storageProviders.putIfAbsent(adapter.getCls(), adapter);
    }

    public Collection<IFindItAdapter> getStorageProviders() {
        return storageProviders.values();
    }

    @SideOnly(Side.CLIENT)
    public boolean isCraftingTerminal(GuiScreen terminal) {
        if (terminal == null) return false;
        if (terminal instanceof GuiContainer gc && gc.inventorySlots != null) {
            return craftingTerminal.containsKey(gc.inventorySlots.getClass());
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    public void addTrackingMissingItem(IAEItemStack is) {
        tracking.add(is);
    }

    @SideOnly(Side.CLIENT)
    public IItemList<IAEItemStack> getTrackingMissingItems() {
        return tracking;
    }

    @SideOnly(Side.CLIENT)
    public void clearTrackingMissingItems() {
        tracking.resetStatus();
    }

    public IPatternTerminalAdapter registerPatternTerminal(IPatternTerminalAdapter adapter) {
        patternTerminal.putIfAbsent(adapter.getContainer(), adapter);
        return adapter;
    }

    public boolean isPatternTerminal() {
        GuiScreen gui = Minecraft.getMinecraft().currentScreen;
        if (gui instanceof GuiRecipe<?>g) {
            return patternTerminal.containsKey(g.getFirstScreen().inventorySlots.getClass());
        }
        return false;
    }

    public IPatternTerminalAdapter getPatternTerminal(Container c) {
        return patternTerminal.getOrDefault(c.getClass(), null);
    }

    public void registerTerminalItem(Class<? extends Item> item, ITerminalHandler terminal) {
        terminalItem.put(item, terminal);
    }

    public ITerminalHandler getOpenTerminalHandler(Class<? extends Item> item) {
        return terminalItem.get(item);
    }
}
