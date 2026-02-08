package com.asdflj.ae2thing.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.client.render.WirelessOverlayRender;
import com.asdflj.ae2thing.common.item.BaseItemBlock;
import com.asdflj.ae2thing.common.tabs.AE2ThingTabs;
import com.asdflj.ae2thing.common.tile.TileWirelessDistributor;
import com.asdflj.ae2thing.loader.IRegister;
import com.asdflj.ae2thing.util.NameConst;

import appeng.util.Platform;
import cpw.mods.fml.common.registry.GameRegistry;

public class BlockWirelessDistributor extends BaseTileBlock implements IRegister<BlockWirelessDistributor> {

    public BlockWirelessDistributor() {
        super(Material.iron);
        this.setBlockName(NameConst.BLOCK_WIRELESS_DISTRIBUTOR);
        this.setBlockTextureName(AE2Thing.MODID + ":" + NameConst.BLOCK_WIRELESS_DISTRIBUTOR);
        setFullBlock(true);
        setOpaque(true);
        setTileEntity(TileWirelessDistributor.class);
    }

    public void setOpaque(boolean opaque) {
        this.isOpaque = opaque;
    }

    public void setFullBlock(boolean full) {
        this.isFullSize = full;
    }

    @Override
    public boolean onActivated(World w, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY,
        float hitZ) {
        if (Platform.isClient()) {
            TileEntity entity = w.getTileEntity(x, y, z);
            if (entity instanceof TileWirelessDistributor tileWirelessDistributor) {
                WirelessOverlayRender.setWirelessDistributor(tileWirelessDistributor);
            }
        }
        return true;
    }

    @Override
    public BlockWirelessDistributor register() {
        GameRegistry.registerBlock(this, BaseItemBlock.class, NameConst.BLOCK_WIRELESS_DISTRIBUTOR);
        GameRegistry.registerTileEntity(TileWirelessDistributor.class, NameConst.BLOCK_WIRELESS_DISTRIBUTOR);
        setCreativeTab(AE2ThingTabs.INSTANCE);
        return this;
    }

    @Override
    public void onBlockPreDestroy(World worldIn, int x, int y, int z, int meta) {
        super.onBlockPreDestroy(worldIn, x, y, z, meta);
    }

    @Override
    public void breakBlock(World w, int x, int y, int z, Block a, int b) {
        if (Platform.isServer()) {
            TileEntity entity = w.getTileEntity(x, y, z);
            if (entity instanceof TileWirelessDistributor tileWirelessDistributor) {
                for (TileWirelessDistributor.GridConnectionWrapper wrapper : tileWirelessDistributor.getGrids()) {
                    wrapper.connection.destroy();
                }
            }
        }
        super.breakBlock(w, x, y, z, a, b);
    }
}
