package com.asdflj.ae2thing.client.render;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import org.lwjgl.opengl.GL11;

import appeng.api.util.DimensionalCoord;
import appeng.api.util.WorldCoord;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

// taken from McJty's McJtyLib
// taken from ae2
public class BlockPosHighlighter {

    private static final List<DimensionalCoord> highlightedBlocks = new ArrayList<>();
    private static long expireHighlightTime;
    private static final int MIN_TIME = 3000;
    private static final int MAX_TIME = MIN_TIME * 10;

    public static void highlightBlocks(EntityPlayer player, List<DimensionalCoord> interfaces, String foundMsg,
        String wrongDimMsg) {
        clear();
        int highlightDuration = MIN_TIME;
        for (DimensionalCoord coord : interfaces) {
            if (player.worldObj.provider.dimensionId == coord.getDimension()) {
                if (foundMsg != null) {
                    player.addChatMessage(new ChatComponentTranslation(foundMsg, coord.x, coord.y, coord.z));
                }
            } else {
                if (wrongDimMsg != null) {
                    player.addChatMessage(new ChatComponentTranslation(wrongDimMsg, coord.getDimension()));
                }
            }
            highlightedBlocks.add(coord);
            highlightDuration = Math.max(
                highlightDuration,
                MathHelper.clamp_int(500 * WorldCoord.getTaxicabDistance(coord, player), MIN_TIME, MAX_TIME));
        }
        expireHighlightTime = System.currentTimeMillis() + highlightDuration;
    }

    private static void clear() {
        highlightedBlocks.clear();
        expireHighlightTime = -1;
    }

    @SubscribeEvent
    public void renderHighlightedBlocks(RenderWorldLastEvent event) {
        if (highlightedBlocks.isEmpty()) {
            return;
        }
        long time = System.currentTimeMillis();
        if (time > expireHighlightTime) {
            clear();
            return;
        }
        if (((time / 500) & 1) == 0) {
            // this does the blinking effect
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        int dimension = mc.theWorld.provider.dimensionId;

        EntityPlayerSP p = mc.thePlayer;
        double doubleX = p.lastTickPosX + (p.posX - p.lastTickPosX) * event.partialTicks;
        double doubleY = p.lastTickPosY + (p.posY - p.lastTickPosY) * event.partialTicks;
        double doubleZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * event.partialTicks;

        for (DimensionalCoord c : highlightedBlocks) {
            if (dimension != c.getDimension()) {
                continue;
            }
            GL11.glPushMatrix();
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            GL11.glLineWidth(3);
            GL11.glTranslated(-doubleX, -doubleY, -doubleZ);

            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_TEXTURE_2D);

            renderHighLightedBlocksOutline(c.x, c.y, c.z);

            GL11.glPopAttrib();
            GL11.glPopMatrix();
        }
    }

    private static void renderHighLightedBlocksOutline(double x, double y, double z) {
        Tessellator tess = Tessellator.instance;
        tess.startDrawing(GL11.GL_LINE_STRIP);

        tess.setColorRGBA_F(1.0f, 0.0f, 0.0f, 1.0f);

        tess.addVertex(x, y, z);
        tess.addVertex(x, y + 1, z);
        tess.addVertex(x, y + 1, z + 1);
        tess.addVertex(x, y, z + 1);
        tess.addVertex(x, y, z);

        tess.addVertex(x + 1, y, z);
        tess.addVertex(x + 1, y + 1, z);
        tess.addVertex(x + 1, y + 1, z + 1);
        tess.addVertex(x + 1, y, z + 1);
        tess.addVertex(x + 1, y, z);

        tess.addVertex(x, y, z);
        tess.addVertex(x + 1, y, z);
        tess.addVertex(x + 1, y, z + 1);
        tess.addVertex(x, y, z + 1);
        tess.addVertex(x, y + 1, z + 1);
        tess.addVertex(x + 1, y + 1, z + 1);
        tess.addVertex(x + 1, y + 1, z);
        tess.addVertex(x + 1, y, z);
        tess.addVertex(x, y, z);
        tess.addVertex(x + 1, y, z);
        tess.addVertex(x + 1, y + 1, z);
        tess.addVertex(x, y + 1, z);
        tess.addVertex(x, y + 1, z + 1);
        tess.addVertex(x + 1, y + 1, z + 1);
        tess.addVertex(x + 1, y, z + 1);
        tess.addVertex(x, y, z + 1);

        tess.draw();
    }
}
