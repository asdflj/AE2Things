package com.asdflj.ae2thing.common.block;

import java.util.List;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public abstract class BaseBlockContainer extends BlockContainer {

    public BaseBlockContainer(Material material) {
        super(material);
    }

    public void addInformation(ItemStack itemStack, EntityPlayer player, List<String> toolTip,
        boolean advancedToolTips) {}

    public ItemStack stack() {
        return new ItemStack(this, 1);
    }
}
