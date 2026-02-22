package com.asdflj.ae2thing.client.textures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import com.asdflj.ae2thing.util.NameConst;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public enum BlockTexture {

    ExIOPort_Side("ex_io_portSide"),
    ExIOPort_Side_Off("ex_io_port_offSide"),
    ExIOPort_Bottom("ex_io_portBottom"),
    ExIOPort_Top("ex_io_port"),
    ExIOPort_Top_Off("ex_io_port_off");

    private final String name;
    public net.minecraft.util.IIcon IIcon;
    public static final HashMap<String, IconWrapper> textureMap = new HashMap<>();

    BlockTexture(final String name) {
        this.name = name;
    }

    public static ResourceLocation GuiTexture(final String string) {
        return null;
    }

    public static class IconWrapper {

        private final List<IIcon> on = new ArrayList<>();
        private final List<IIcon> off = new ArrayList<>();

        public List<IIcon> get() {
            return get(true);
        }

        public List<IIcon> get(boolean powered) {
            return powered ? on : off;
        }

        public void add(IIcon icon, boolean powered) {
            if (powered) {
                on.add(icon);
            }
            off.add(icon);
        }

        public void add(IIcon icon) {
            add(icon, true);
        }
    }

    @SideOnly(Side.CLIENT)
    public static IIcon getMissing() {
        return ((TextureMap) Minecraft.getMinecraft()
            .getTextureManager()
            .getTexture(TextureMap.locationBlocksTexture)).getAtlasSprite("missingno");
    }

    public String getName() {
        return this.name;
    }

    public IIcon getIcon() {
        return this.IIcon;
    }

    public void registerIcon(final TextureMap map) {
        this.IIcon = map.registerIcon(NameConst.RES_KEY + this.name);
    }

    public static void registerIcon(final TextureMap map, String name, String fullName, boolean powered) {
        IIcon icon = map.registerIcon(fullName);
        IconWrapper wrapper = textureMap.getOrDefault(name, new IconWrapper());
        wrapper.add(icon, powered);
        textureMap.putIfAbsent(name, wrapper);
    }

    public static void registerIcon(final TextureMap map, String name, String fullName) {
        registerIcon(map, name, fullName, true);
    }
}
