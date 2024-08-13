package com.asdflj.ae2thing.client.render;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import com.asdflj.ae2thing.client.textures.Texture;
import com.asdflj.ae2thing.common.block.BlockExIOPort;
import com.asdflj.ae2thing.common.tile.TileExIOPort;

import appeng.client.render.BaseBlockRender;
import appeng.client.render.BlockRenderInfo;

public class RenderBlockExIOPort extends BaseBlockRender<BlockExIOPort, TileExIOPort> {

    public RenderBlockExIOPort() {
        super(false, 20);
    }

    @Override
    public boolean renderInWorld(final BlockExIOPort block, final IBlockAccess world, final int x, final int y,
        final int z, final RenderBlocks renderer) {
        final TileExIOPort ti = block.getTileEntity(world, x, y, z);
        final BlockRenderInfo info = block.getRendererInstance();
        if (ti != null) {
            final IIcon bottom = Texture.Block_ExIOPort_Bottom.getIcon();
            final IIcon side;
            final IIcon top;
            if (ti.isActive()) {
                side = Texture.Block_ExIOPort_Side.getIcon();
                top = Texture.Block_ExIOPort_Top.getIcon();
            } else {
                side = Texture.Block_ExIOPort_Side_Off.getIcon();
                top = Texture.Block_ExIOPort_Top_Off.getIcon();
            }
            info.setTemporaryRenderIcons(top, bottom, side, side, side, side);
        }

        final boolean fz = super.renderInWorld(block, world, x, y, z, renderer);

        info.setTemporaryRenderIcon(null);

        return fz;
    }
}
