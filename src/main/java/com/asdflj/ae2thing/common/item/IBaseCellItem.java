package com.asdflj.ae2thing.common.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IBaseCellItem {

    void addToolTips(ItemStack stack, EntityPlayer player, List<String> lines, boolean displayMoreInfo);

}
