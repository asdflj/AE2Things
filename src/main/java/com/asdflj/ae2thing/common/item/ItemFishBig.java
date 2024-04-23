package com.asdflj.ae2thing.common.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.common.block.BaseBlockContainer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemFishBig extends ItemBlock {

    private final BaseBlockContainer blockType;

    public ItemFishBig(Block id) {
        super(id);
        this.blockType = (BaseBlockContainer) id;
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public void addInformation(final ItemStack itemStack, final EntityPlayer player, final List toolTip,
        final boolean advancedToolTips) {
        blockType.addInformation(itemStack, player, toolTip, advancedToolTips);
    }
}
