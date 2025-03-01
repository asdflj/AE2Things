package com.asdflj.ae2thing.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

import baubles.api.BaublesApi;

public class BaublesUtil {

    public static IInventory getBaublesInv(EntityPlayer player) {
        return BaublesApi.getBaubles(player);
    }
}
