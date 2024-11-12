package com.asdflj.ae2thing.common.block;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import appeng.block.AEBaseTileBlock;
import appeng.core.features.ActivityState;
import appeng.core.features.BlockStackSrc;
import appeng.tile.AEBaseTile;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class BaseTileBlock extends AEBaseTileBlock {

    public BaseTileBlock(Material mat) {
        super(mat);
    }

    public ItemStack stack() {
        return new ItemStack(this, 1);
    }

    @Override
    public void setTileEntity(final Class<? extends TileEntity> clazz) {
        AEBaseTile.registerTileItem(clazz, new BlockStackSrc(this, 0, ActivityState.Enabled));
        super.setTileEntity(clazz);
    }

    public void addCheckedInformation(ItemStack itemStack, EntityPlayer player, List<String> toolTip,
        boolean advancedToolTips) {
        this.addInformation(itemStack, player, toolTip, advancedToolTips);
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack itemStack, final EntityPlayer player, final List<String> toolTip,
        final boolean advancedToolTips) {}

}
