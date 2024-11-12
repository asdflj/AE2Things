package com.asdflj.ae2thing.common.block;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.client.render.RenderBlockExIOPort;
import com.asdflj.ae2thing.common.tabs.AE2ThingTabs;
import com.asdflj.ae2thing.common.tile.TileExIOPort;
import com.asdflj.ae2thing.loader.IRegister;
import com.asdflj.ae2thing.util.NameConst;

import appeng.block.AEBaseItemBlock;
import appeng.block.storage.BlockIOPort;
import appeng.core.features.ActivityState;
import appeng.core.features.BlockStackSrc;
import appeng.tile.AEBaseTile;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockExIOPort extends BlockIOPort implements IRegister<BlockExIOPort> {

    public BlockExIOPort() {
        super();
        this.setBlockName(NameConst.BLOCK_EX_IO_PORT);
        this.setBlockTextureName(AE2Thing.MODID + ":" + NameConst.BLOCK_EX_IO_PORT);
        setFullBlock(true);
        setOpaque(true);
        this.setTileEntity(TileExIOPort.class);
    }

    public void setOpaque(boolean opaque) {
        this.isOpaque = opaque;
    }

    public void setFullBlock(boolean full) {
        this.isFullSize = full;
    }

    @Override
    public BlockExIOPort register() {
        GameRegistry.registerBlock(this, AEBaseItemBlock.class, NameConst.BLOCK_EX_IO_PORT);
        GameRegistry.registerTileEntity(TileExIOPort.class, NameConst.BLOCK_EX_IO_PORT);
        setCreativeTab(AE2ThingTabs.INSTANCE);
        return this;
    }

    @Override
    public void setTileEntity(final Class<? extends TileEntity> clazz) {
        AEBaseTile.registerTileItem(clazz, new BlockStackSrc(this, 0, ActivityState.Enabled));
        super.setTileEntity(clazz);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected RenderBlockExIOPort getRenderer() {
        return new RenderBlockExIOPort();
    }

    public ItemStack stack() {
        return new ItemStack(this, 1);
    }
}
