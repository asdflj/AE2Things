package com.asdflj.ae2thing.common.storage;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

@FunctionalInterface
public interface ModInv {

    List<IInventory> getInv(EntityPlayer player);
}
