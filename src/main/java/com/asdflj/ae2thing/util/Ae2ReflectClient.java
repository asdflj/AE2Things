package com.asdflj.ae2thing.util;

import static com.glodblock.github.util.Ae2Reflect.readField;
import static com.glodblock.github.util.Ae2Reflect.reflectField;
import static com.glodblock.github.util.Ae2Reflect.reflectMethod;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.glodblock.github.client.gui.GuiDualInterface;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.implementations.GuiCraftingStatus;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.client.me.ItemRepo;
import appeng.helpers.IInterfaceHost;
import codechicken.nei.SearchField;
import codechicken.nei.util.TextHistory;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Ae2ReflectClient {

    private static final Field fSearchField_history;
    private static final Field fTextHistory_history;
    private static final Method mAEBaseGui_drawSlot;
    private static final Field fItemRepo_view;
    private static final Field fItemRepo_dsp;
    private static final Field fItemRepo_list;
    private static final Field fGuiDualInterface_host;

    static {
        try {
            fItemRepo_view = reflectField(ItemRepo.class, "view");
            fItemRepo_dsp = reflectField(ItemRepo.class, "dsp");
            fItemRepo_list = reflectField(ItemRepo.class, "list");
            fGuiDualInterface_host = reflectField(GuiDualInterface.class, "host");
            fSearchField_history = reflectField(SearchField.class, "history");
            fTextHistory_history = reflectField(TextHistory.class, "history");
            mAEBaseGui_drawSlot = reflectMethod(AEBaseGui.class, "drawSlot", Slot.class);
        } catch (NoSuchFieldException | NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Failed to initialize AE2 reflection hacks!", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Slot> getInventorySlots(AEBaseGui gui) {
        return com.glodblock.github.util.Ae2ReflectClient.getInventorySlots(gui);
    }

    public static void rewriteIcon(GuiCraftingStatus gui, ItemStack icon) {
        com.glodblock.github.util.Ae2ReflectClient.rewriteIcon(gui, icon);
    }

    public static GuiTabButton getOriginalGuiButton(GuiCraftingStatus gui) {
        return com.glodblock.github.util.Ae2ReflectClient.getOriginalGuiButton(gui);
    }

    public static Set<Slot> getDragClick(AEBaseGui gui) {
        return com.glodblock.github.util.Ae2ReflectClient.getDragClick(gui);
    }

    public static TextHistory getHistory(SearchField searchField) {
        return readField(searchField, fSearchField_history);
    }

    public static List<String> getHistoryList(TextHistory textHistory) {
        return readField(textHistory, fTextHistory_history);
    }

    public static ArrayList<IAEItemStack> getView(ItemRepo repo) {
        return readField(repo, fItemRepo_view);
    }

    public static ArrayList<ItemStack> getDsp(ItemRepo repo) {
        return readField(repo, fItemRepo_dsp);
    }

    public static IItemList<IAEItemStack> getList(ItemRepo repo) {
        return readField(repo, fItemRepo_list);
    }

    public static void drawSlot(AEBaseGui gui, Slot slot) {
        try {
            mAEBaseGui_drawSlot.invoke(gui, slot);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to invoke method: " + mAEBaseGui_drawSlot, e);
        }
    }

    public static IInterfaceHost getHost(GuiDualInterface gui) {
        return readField(gui, fGuiDualInterface_host);
    }

}
