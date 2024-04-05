package com.asdflj.ae2thing.inventory.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface IItemInventory {

    Object getInventory(ItemStack stack, World world, int x, int y, int z, EntityPlayer player);
}
