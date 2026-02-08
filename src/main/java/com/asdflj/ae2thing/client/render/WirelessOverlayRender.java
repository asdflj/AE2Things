package com.asdflj.ae2thing.client.render;

import net.bdew.ae2stuff.misc.WorldOverlayRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.MovingObjectPosition;

import org.lwjgl.opengl.GL11;

import com.asdflj.ae2thing.common.tile.TileWirelessDistributor;
import com.asdflj.ae2thing.util.BlockPos;

import appeng.api.util.DimensionalCoord;

public class WirelessOverlayRender implements WorldOverlayRenderer {

    private static TileWirelessDistributor tile;
    private static final int timeout = 1000 * 10;
    private static long startTime = 0;

    public static void setWirelessDistributor(TileWirelessDistributor tileWirelessDistributor) {
        tile = tileWirelessDistributor;
        startTime = System.currentTimeMillis();
    }

    @Override
    public void doRender(float partialTicks, double viewX, double viewY, double viewZ) {
        var mop = Minecraft.getMinecraft().objectMouseOver;
        if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            BlockPos pos = new BlockPos(mop.blockX, mop.blockY, mop.blockZ, Minecraft.getMinecraft().theWorld);
            if (pos.getTileEntity() != null && pos.getTileEntity() instanceof TileWirelessDistributor tileDistributor) {
                draw(tileDistributor);
            } else if (tile != null) {
                if (startTime + timeout > System.currentTimeMillis()) {
                    draw(tile);
                } else {
                    tile = null;
                }
            }
        }
    }

    private void draw(TileWirelessDistributor tileDistributor) {
        if (tileDistributor == null) return;
        BlockPos pos = new BlockPos(tileDistributor);
        for (DimensionalCoord dimensionalCoord : tileDistributor.getDimensionalCoords()) {
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT);

            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
            GL11.glLineWidth(4.0f);

            var tess = Tessellator.instance;
            tess.startDrawing(GL11.GL_LINES);
            tess.setColorRGBA_F(0, 0, 1, 1);
            tess.addVertex(pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d);
            tess.addVertex(dimensionalCoord.x + 0.5d, dimensionalCoord.y + 0.5d, dimensionalCoord.z + 0.5d);
            tess.draw();

            GL11.glPopAttrib();
        }
    }

}
