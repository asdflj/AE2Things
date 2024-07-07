package com.asdflj.ae2thing.common.block;

import com.asdflj.ae2thing.common.tabs.AE2ThingTabs;
import com.asdflj.ae2thing.common.tile.TileInfusionInterface;
import com.asdflj.ae2thing.util.NameConst;
import com.glodblock.github.common.block.BlockFluidInterface;

import appeng.block.AEBaseItemBlock;
import cpw.mods.fml.common.registry.GameRegistry;

public class BlockInfusionInterface extends BlockFluidInterface {

    public BlockInfusionInterface() {
        super();
        setTileEntity(TileInfusionInterface.class);
    }

    @Override
    public BlockInfusionInterface register() {
        GameRegistry.registerBlock(this, AEBaseItemBlock.class, NameConst.BLOCK_INFUSION_INTERFACE);
        GameRegistry.registerTileEntity(TileInfusionInterface.class, NameConst.BLOCK_INFUSION_INTERFACE);
        setCreativeTab(AE2ThingTabs.INSTANCE);
        return this;
    }
}
