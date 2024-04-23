package com.asdflj.ae2thing.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.opengl.GL11;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.common.tile.TileFishBig;
import com.asdflj.ae2thing.loader.ItemAndBlockHolder;

import cpw.mods.fml.client.registry.ClientRegistry;

public class ItemFishBigRender implements IItemRenderer {

    public static final ResourceLocation leg = new ResourceLocation(AE2Thing.MODID, "textures/blocks/fishbig/leg.png");
    public static final ResourceLocation leg2 = new ResourceLocation(
        AE2Thing.MODID,
        "textures/blocks/fishbig/leg2.png");
    public static final ResourceLocation arm = new ResourceLocation(AE2Thing.MODID, "textures/blocks/fishbig/arm.png");
    public static final ResourceLocation body = new ResourceLocation(
        AE2Thing.MODID,
        "textures/blocks/fishbig/body.png");
    public static final ResourceLocation button = new ResourceLocation(
        AE2Thing.MODID,
        "textures/blocks/fishbig/button.png");
    public static final ResourceLocation hair = new ResourceLocation(
        AE2Thing.MODID,
        "textures/blocks/fishbig/hair.png");
    public static final ResourceLocation hair2 = new ResourceLocation(
        AE2Thing.MODID,
        "textures/blocks/fishbig/hair2.png");
    public static final ResourceLocation head = new ResourceLocation(
        AE2Thing.MODID,
        "textures/blocks/fishbig/head.png");
    public static IModelCustom modelFishBig = AdvancedModelLoader.loadModel(AE2Thing.resource("models/fishbig.obj"));

    public ItemFishBigRender() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileFishBig.class, new RenderBlockFishBig());
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ItemAndBlockHolder.BLOCK_FISH_BIG), this);

    }

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        GL11.glPushMatrix();
        boolean renderHair = true;
        GL11.glScalef(1.1f, 1.1f, 1.1f);
        GL11.glRotated(180, 0, 1, 0);
        switch (type) {
            case EQUIPPED_FIRST_PERSON:
                GL11.glTranslatef(-0.2f, 0.3f, -0.4f);
                GL11.glRotated(-90, 0, 1, 0);
                break;
            case INVENTORY:
                renderHair = false;
                break;
            default:
                GL11.glTranslatef(0, 0.7F, -0.5F);
                GL11.glRotated(-90, 0, 1, 0);
                break;
        }
        render(renderHair);
        GL11.glPopMatrix();
    }

    public static void render(boolean renderHair) {
        Minecraft.getMinecraft().renderEngine.bindTexture(hair2);
        modelFishBig.renderPart("hair2");
        Minecraft.getMinecraft().renderEngine.bindTexture(head);
        modelFishBig.renderPart("head");
        Minecraft.getMinecraft().renderEngine.bindTexture(leg);
        modelFishBig.renderPart("leg");
        Minecraft.getMinecraft().renderEngine.bindTexture(leg2);
        modelFishBig.renderPart("leg2");
        Minecraft.getMinecraft().renderEngine.bindTexture(arm);
        modelFishBig.renderPart("arm");
        Minecraft.getMinecraft().renderEngine.bindTexture(body);
        modelFishBig.renderPart("body");
        Minecraft.getMinecraft().renderEngine.bindTexture(button);
        modelFishBig.renderPart("button");
        if (renderHair) {
            Minecraft.getMinecraft().renderEngine.bindTexture(hair);
            modelFishBig.renderPart("hair");
        }
    }
}
