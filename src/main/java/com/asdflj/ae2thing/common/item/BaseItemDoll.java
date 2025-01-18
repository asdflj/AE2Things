package com.asdflj.ae2thing.common.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.common.block.BaseDollBlockContainer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BaseItemDoll extends ItemBlock {

    private final BaseDollBlockContainer blockType;

    public BaseItemDoll(Block id) {
        super(id);
        this.blockType = (BaseDollBlockContainer) id;
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public void addInformation(final ItemStack itemStack, final EntityPlayer player, final List toolTip,
        final boolean advancedToolTips) {
        blockType.addInformation(itemStack, player, toolTip, advancedToolTips);
    }
}
