package com.asdflj.ae2thing.common.block;

import net.minecraft.block.material.Material;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.common.tabs.AE2ThingTabs;
import com.asdflj.ae2thing.common.tile.TileEssenceDiscretizer;
import com.asdflj.ae2thing.loader.IRegister;
import com.asdflj.ae2thing.util.NameConst;

import appeng.block.AEBaseItemBlock;
import appeng.block.AEBaseTileBlock;
import cpw.mods.fml.common.registry.GameRegistry;

public class BlockEssenceDiscretizer extends AEBaseTileBlock implements IRegister<BlockEssenceDiscretizer> {

    public BlockEssenceDiscretizer() {
        super(Material.iron);
        this.setBlockName(NameConst.BLOCK_ESSENCE_DISCRETIZER);
        this.setBlockTextureName(AE2Thing.MODID + ":" + NameConst.BLOCK_ESSENCE_DISCRETIZER);
        setFullBlock(true);
        setOpaque(true);
        setTileEntity(TileEssenceDiscretizer.class);
    }

    public void setOpaque(boolean opaque) {
        this.isOpaque = opaque;
    }

    public void setFullBlock(boolean full) {
        this.isFullSize = full;
    }

    @Override
    public BlockEssenceDiscretizer register() {
        GameRegistry.registerBlock(this, AEBaseItemBlock.class, NameConst.BLOCK_ESSENCE_DISCRETIZER);
        GameRegistry.registerTileEntity(TileEssenceDiscretizer.class, NameConst.BLOCK_ESSENCE_DISCRETIZER);
        setCreativeTab(AE2ThingTabs.INSTANCE);
        return this;
    }

}
