package com.asdflj.ae2thing.util;

import static com.glodblock.github.util.Ae2Reflect.readField;
import static com.glodblock.github.util.Ae2Reflect.reflectField;
import static com.glodblock.github.util.Ae2Reflect.reflectMethod;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import appeng.api.storage.IMEInventory;
import appeng.container.implementations.ContainerInterfaceTerminal;
import appeng.container.slot.SlotCraftingTerm;
import appeng.me.storage.MEInventoryHandler;
import appeng.me.storage.MEPassThrough;
import appeng.util.prioitylist.IPartitionList;

public class Ae2Reflect {

    private static final Field fAEPass_internal;
    private static final Field fAEInv_partitionList;
    private static final Field fContainerInterfaceTerminal_tracked;
    private static final Method mSlotCraftingTerm_makeItem;

    static {
        try {
            fAEPass_internal = reflectField(MEPassThrough.class, "internal");
            fAEInv_partitionList = reflectField(MEInventoryHandler.class, "myPartitionList");
            fContainerInterfaceTerminal_tracked = reflectField(ContainerInterfaceTerminal.class, "tracked");
            mSlotCraftingTerm_makeItem = reflectMethod(
                SlotCraftingTerm.class,
                "makeItem",
                EntityPlayer.class,
                ItemStack.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize AE2 reflection hacks!", e);
        }
    }

    public static IPartitionList<?> getPartitionList(MEInventoryHandler<?> me) {
        return readField(me, fAEInv_partitionList);
    }

    public static IMEInventory<?> getInternal(MEPassThrough<?> me) {
        return readField(me, fAEPass_internal);
    }

    public static Map getTracked(ContainerInterfaceTerminal obj) {
        return readField(obj, fContainerInterfaceTerminal_tracked);
    }

    public static void makeItem(SlotCraftingTerm obj, EntityPlayer player, ItemStack stack) {
        try {
            mSlotCraftingTerm_makeItem.invoke(obj, player, stack);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to invoke method: " + mSlotCraftingTerm_makeItem, e);
        }
    }

}
