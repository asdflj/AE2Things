package com.asdflj.ae2thing.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import com.asdflj.ae2thing.common.item.ItemFishBig;
import com.asdflj.ae2thing.common.tabs.AE2ThingTabs;
import com.asdflj.ae2thing.common.tile.TileFishBig;
import com.asdflj.ae2thing.loader.IRegister;
import com.asdflj.ae2thing.util.NameConst;

import cpw.mods.fml.common.registry.GameRegistry;

public class BlockFishBig extends BaseBlockContainer implements IRegister<BlockFishBig> {

    public BlockFishBig() {
        super(Material.iron);
        this.setBlockName(NameConst.BLOCK_FISHBIG);
        this.setHardness(2.0f);
        this.setResistance(10.0F);
    }

    @Override
    public BlockFishBig register() {
        GameRegistry.registerBlock(this, ItemFishBig.class, NameConst.BLOCK_FISHBIG);
        GameRegistry.registerTileEntity(TileFishBig.class, NameConst.BLOCK_FISHBIG);
        setCreativeTab(AE2ThingTabs.INSTANCE);
        return this;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemstack) {
        int l = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;

        if (l == 0) {
            world.setBlockMetadataWithNotify(x, y, z, 2, 2);
        }

        if (l == 1) {
            world.setBlockMetadataWithNotify(x, y, z, 5, 2);
        }

        if (l == 2) {
            world.setBlockMetadataWithNotify(x, y, z, 3, 2);
        }

        if (l == 3) {
            world.setBlockMetadataWithNotify(x, y, z, 4, 2);
        }
    }

    @Override
    public int getRenderType() {
        return -1;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileFishBig();
    }

}
