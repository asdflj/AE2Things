package com.asdflj.ae2thing.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.asdflj.ae2thing.api.AE2ThingAPI;

import appeng.util.Platform;

public class Util {

    public static int findItemStack(EntityPlayer player, ItemStack itemStack) {
        for (int x = 0; x < player.inventory.mainInventory.length; x++) {
            ItemStack item = player.inventory.mainInventory[x];
            if (item == null) continue;
            if (Platform.isSameItemPrecise(item, itemStack)) {
                return x;
            }
        }
        return -1;
    }

    public static List<Integer> getBackpackSlot(EntityPlayer player) {
        List<Integer> result = new ArrayList<>();
        for (int x = 0; x < player.inventory.mainInventory.length; x++) {
            ItemStack item = player.inventory.mainInventory[x];
            if (item == null || item.getItem() == null) continue;
            if (AE2ThingAPI.instance()
                .isBackpackItem(item)) {
                result.add(x);
            }
        }
        return result;
    }

    public static void writeItemStackToNBT(ItemStack itemStack, NBTTagCompound tag) {
        itemStack.writeToNBT(tag);
        tag.setInteger("Count", itemStack.stackSize);
    }

    public static ItemStack loadItemStackFromNBT(NBTTagCompound tag) {
        ItemStack itemStack = ItemStack.loadItemStackFromNBT(tag);
        if (itemStack == null) return null;
        itemStack.stackSize = tag.getInteger("Count");
        return itemStack;
    }
}
