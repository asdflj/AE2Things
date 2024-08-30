package com.asdflj.ae2thing.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import appeng.api.networking.energy.IEnergySource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.AEBaseContainer;
import appeng.container.implementations.ContainerInterfaceTerminal;
import appeng.me.storage.MEInventoryHandler;
import appeng.me.storage.MEPassThrough;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.storage.TileIOPort;
import appeng.util.ConfigManager;
import appeng.util.prioitylist.IPartitionList;

public class Ae2Reflect {

    private static final Field fAEPass_internal;
    private static final Field fAEInv_partitionList;
    private static final Field fIOPort_cells;
    private static final Method mIOPort_getInv;
    private static final Field fIOPort_manager;
    private static final Method mIOPort_transferContents;
    private static final Field fIOPort_FLUID_MULTIPLIER;
    private static final Method mIOPort_shouldMove;
    private static final Method mIOPort_moveSlot;
    private static final Method mAEBaseContainer_addSlotToContainer;
    private static final Field fContainerInterfaceTerminal_tracked;
    // private static final Field fContainerInterfaceTerminal_InvTracker_id;

    static {
        try {
            fAEPass_internal = reflectField(MEPassThrough.class, "internal");
            fAEInv_partitionList = reflectField(MEInventoryHandler.class, "myPartitionList");
            fIOPort_cells = reflectField(TileIOPort.class, "cells");
            mIOPort_getInv = reflectMethod(TileIOPort.class, "getInv", ItemStack.class, StorageChannel.class);
            fIOPort_manager = reflectField(TileIOPort.class, "manager");
            mIOPort_transferContents = reflectMethod(
                TileIOPort.class,
                "transferContents",
                IEnergySource.class,
                IMEInventory.class,
                IMEInventory.class,
                long.class,
                StorageChannel.class);
            fIOPort_FLUID_MULTIPLIER = reflectField(TileIOPort.class, "FLUID_MULTIPLIER");
            mIOPort_shouldMove = reflectMethod(TileIOPort.class, "shouldMove", IMEInventory.class, IMEInventory.class);
            mIOPort_moveSlot = reflectMethod(TileIOPort.class, "moveSlot", int.class);
            mAEBaseContainer_addSlotToContainer = reflectMethod(
                AEBaseContainer.class,
                "addSlotToContainer",
                Slot.class);
            fContainerInterfaceTerminal_tracked = reflectField(ContainerInterfaceTerminal.class, "tracked");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize AE2 reflection hacks!", e);
        }
    }

    public static Method reflectMethod(Class<?> owner, String name, Class<?>... paramTypes)
        throws NoSuchMethodException {
        Method m = owner.getDeclaredMethod(name, paramTypes);
        m.setAccessible(true);
        return m;
    }

    public static Field reflectField(Class<?> owner, String... names) throws NoSuchFieldException {
        Field f = null;
        for (String name : names) {
            try {
                f = owner.getDeclaredField(name);
                // IntelliJ misses that the exception is ignored and thus "f" can indeed be null.
                // noinspection ConstantValue
                if (f != null) break;
            } catch (NoSuchFieldException ignore) {}
        }
        if (f == null) throw new NoSuchFieldException("Can't find field from " + Arrays.toString(names));
        f.setAccessible(true);
        return f;
    }

    @SuppressWarnings("unchecked")
    public static <T> T readField(Object owner, Field field) {
        try {
            return (T) field.get(owner);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read field: " + field);
        }
    }

    public static void writeField(Object owner, Field field, Object value) {
        try {
            field.set(owner, value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write field: " + field);
        }
    }

    public static IPartitionList<?> getPartitionList(MEInventoryHandler<?> me) {
        return Ae2Reflect.readField(me, fAEInv_partitionList);
    }

    public static IMEInventory<?> getInternal(MEPassThrough<?> me) {
        return Ae2Reflect.readField(me, fAEPass_internal);
    }

    public static AppEngInternalInventory getCells(TileIOPort obj) {
        return Ae2Reflect.readField(obj, fIOPort_cells);
    }

    public static ConfigManager getManager(TileIOPort obj) {
        return Ae2Reflect.readField(obj, fIOPort_manager);
    }

    public static IMEInventory getInv(TileIOPort obj, ItemStack is, StorageChannel channel) {
        try {
            return (IMEInventory) mIOPort_getInv.invoke(obj, is, channel);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to invoke method: " + mIOPort_getInv, e);
        }
    }

    public static long getFluidMultiplier(TileIOPort obj) {
        return Ae2Reflect.readField(obj, fIOPort_FLUID_MULTIPLIER);
    }

    public static boolean shouldMove(TileIOPort obj, final IMEInventory<IAEItemStack> itemInv,
        final IMEInventory<IAEFluidStack> fluidInv) {
        try {
            return (boolean) mIOPort_shouldMove.invoke(obj, itemInv, fluidInv);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to invoke method: " + mIOPort_shouldMove, e);
        }
    }

    public static boolean moveSlot(TileIOPort obj, int x) {
        try {
            return (boolean) mIOPort_moveSlot.invoke(obj, x);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to invoke method: " + mIOPort_moveSlot, e);
        }
    }

    public static long transferContents(TileIOPort obj, final IEnergySource energy, final IMEInventory src,
        final IMEInventory destination, long itemsToMove, final StorageChannel chan) {
        try {
            return (long) mIOPort_transferContents.invoke(obj, energy, src, destination, itemsToMove, chan);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to invoke method: " + mIOPort_transferContents, e);
        }
    }

    public static Slot addSlotToContainer(AEBaseContainer obj, Slot slot) {
        try {
            return (Slot) mAEBaseContainer_addSlotToContainer.invoke(obj, slot);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to invoke method: " + mIOPort_transferContents, e);
        }
    }

    public static Map getTracked(ContainerInterfaceTerminal obj) {
        return Ae2Reflect.readField(obj, fContainerInterfaceTerminal_tracked);
    }

}
