package com.asdflj.ae2thing.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import net.minecraft.inventory.Slot;

import appeng.client.gui.AEBaseGui;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Ae2ReflectClient {

    private static final Field fGui_drag;
    private static final Method mGuiPatternTerm_inventorySlots;

    static {
        try {
            fGui_drag = Ae2Reflect.reflectField(AEBaseGui.class, "drag_click");
            mGuiPatternTerm_inventorySlots = Ae2Reflect.reflectMethod(AEBaseGui.class, "getInventorySlots");
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

}
