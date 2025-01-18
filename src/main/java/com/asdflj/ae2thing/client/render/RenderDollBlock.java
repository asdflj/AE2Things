package com.asdflj.ae2thing.client.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import com.asdflj.ae2thing.common.block.BaseDollBlockContainer;

public class RenderDollBlock extends TileEntitySpecialRenderer {

    private final ItemDollRender render;

    public RenderDollBlock(ItemDollRender itemDollRender) {
        super();
        this.render = itemDollRender;

    }

    @Override
    public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float partialTickTime) {
        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5f, y + 0.6f, z + 0.5f);
        int orientation = tileentity.getBlockMetadata();
        if (orientation == 4) {
            GL11.glRotatef(90, 0, 1, 0);
        } else if (orientation == 5) {
            GL11.glRotatef(-90, 0, 1, 0);
        } else if (orientation == 3) {
            GL11.glRotatef(180, 0, 1, 0);
        }
        if (tileentity.blockType instanceof BaseDollBlockContainer doll) {
            this.render.render(true, doll.name);
        }

        GL11.glPopMatrix();
    }
}
