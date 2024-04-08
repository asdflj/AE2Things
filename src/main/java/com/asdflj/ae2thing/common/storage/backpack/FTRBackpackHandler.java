package com.asdflj.ae2thing.common.storage.backpack;

import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import forestry.storage.inventory.ItemInventoryBackpack;
import forestry.storage.items.ItemBackpack;

public class FTRBackpackHandler extends BaseBackpackHandler {

    public FTRBackpackHandler(EntityPlayer player, ItemStack is) {
        super(
            new ItemInventoryBackpack(
                player,
                ((ItemBackpack) Objects.requireNonNull(is.getItem())).getBackpackSize(),
                is));
    }

}
