package com.asdflj.ae2thing.common.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.common.block.BaseTileBlock;

import appeng.block.AEBaseItemBlock;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BaseItemBlock extends AEBaseItemBlock {

    private final BaseTileBlock blockType;

    public BaseItemBlock(Block id) {
        super(id);
        blockType = (BaseTileBlock) id;
    }

    @SideOnly(Side.CLIENT)
    public void addCheckedInformation(final ItemStack itemStack, final EntityPlayer player, final List<String> toolTip,
        final boolean advancedToolTips) {
        blockType.addCheckedInformation(itemStack, player, toolTip, advancedToolTips);
        super.addCheckedInformation(itemStack, player, toolTip, advancedToolTips);
    }
}
