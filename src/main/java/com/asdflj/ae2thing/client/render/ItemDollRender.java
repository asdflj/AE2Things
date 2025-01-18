package com.asdflj.ae2thing.client.render;

import java.util.HashMap;

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
import com.asdflj.ae2thing.common.block.BaseDollBlockContainer;
import com.asdflj.ae2thing.common.tile.TileDoll;

import cpw.mods.fml.client.registry.ClientRegistry;

public class ItemDollRender implements IItemRenderer {

    public static class Resource {

        private final ResourceLocation leg;
        private final ResourceLocation leg2;
        private final ResourceLocation arm;
        private final ResourceLocation body;
        private final ResourceLocation button;
        private final ResourceLocation hair;
        private final ResourceLocation hair2;
        private final ResourceLocation head;

        public Resource(String name) {
            leg = new ResourceLocation(AE2Thing.MODID, String.format("textures/blocks/%s/leg.png", name));
            leg2 = new ResourceLocation(AE2Thing.MODID, String.format("textures/blocks/%s/leg2.png", name));
            arm = new ResourceLocation(AE2Thing.MODID, String.format("textures/blocks/%s/arm.png", name));
            body = new ResourceLocation(AE2Thing.MODID, String.format("textures/blocks/%s/body.png", name));
            button = new ResourceLocation(AE2Thing.MODID, String.format("textures/blocks/%s/button.png", name));
            hair = new ResourceLocation(AE2Thing.MODID, String.format("textures/blocks/%s/hair.png", name));
            hair2 = new ResourceLocation(AE2Thing.MODID, String.format("textures/blocks/%s/hair2.png", name));
            head = new ResourceLocation(AE2Thing.MODID, String.format("textures/blocks/%s/head.png", name));
        }
    }

    public static IModelCustom modelDoll = AdvancedModelLoader.loadModel(AE2Thing.resource("models/doll.obj"));
    public static HashMap<String, Resource> resources = new HashMap<>();
    private final String name;

    public ItemDollRender(BaseDollBlockContainer blockContainer) {
        this.name = blockContainer.name;
        ClientRegistry.bindTileEntitySpecialRenderer(TileDoll.class, new RenderDollBlock(this));
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(blockContainer), this);
        resources.put(name, new Resource(name));
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
        render(renderHair, this.name);
        GL11.glPopMatrix();
    }

    public void render(boolean renderHair, String name) {
        Minecraft.getMinecraft().renderEngine.bindTexture(resources.get(name).hair2);
        modelDoll.renderPart("hair2");
        Minecraft.getMinecraft().renderEngine.bindTexture(resources.get(name).head);
        modelDoll.renderPart("head");
        Minecraft.getMinecraft().renderEngine.bindTexture(resources.get(name).leg);
        modelDoll.renderPart("leg");
        Minecraft.getMinecraft().renderEngine.bindTexture(resources.get(name).leg2);
        modelDoll.renderPart("leg2");
        Minecraft.getMinecraft().renderEngine.bindTexture(resources.get(name).arm);
        modelDoll.renderPart("arm");
        Minecraft.getMinecraft().renderEngine.bindTexture(resources.get(name).body);
        modelDoll.renderPart("body");
        Minecraft.getMinecraft().renderEngine.bindTexture(resources.get(name).button);
        modelDoll.renderPart("button");
        if (renderHair) {
            Minecraft.getMinecraft().renderEngine.bindTexture(resources.get(name).hair);
            modelDoll.renderPart("hair");
        }
    }
}
