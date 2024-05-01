package com.asdflj.ae2thing.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import net.minecraft.inventory.Slot;

import appeng.client.gui.AEBaseGui;
import codechicken.nei.SearchField;
import codechicken.nei.util.TextHistory;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Ae2ReflectClient {

    private static final Field fGui_drag;
    private static final Method mGuiPatternTerm_inventorySlots;
    private static final Field fSearchField_history;
    private static final Field fTextHistory_history;

    static {
        try {
            fGui_drag = Ae2Reflect.reflectField(AEBaseGui.class, "drag_click");
            mGuiPatternTerm_inventorySlots = Ae2Reflect.reflectMethod(AEBaseGui.class, "getInventorySlots");
            fSearchField_history = Ae2Reflect.reflectField(SearchField.class, "history");
            fTextHistory_history = Ae2Reflect.reflectField(TextHistory.class, "history");
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            throw new IllegalStateException("Failed to initialize AE2 reflection hacks!", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Slot> getInventorySlots(AEBaseGui gui) {
        try {
            return (List<Slot>) mGuiPatternTerm_inventorySlots.invoke(gui);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to invoke method: " + mGuiPatternTerm_inventorySlots, e);
        }
    }

    public static Set<Slot> getDragClick(AEBaseGui gui) {
        return Ae2Reflect.readField(gui, fGui_drag);
    }

    public static TextHistory getHistory(SearchField searchField) {
        return Ae2Reflect.readField(searchField, fSearchField_history);
    }

    public static List<String> getHistoryList(TextHistory textHistory) {
        return Ae2Reflect.readField(textHistory, fTextHistory_history);
    }

}
