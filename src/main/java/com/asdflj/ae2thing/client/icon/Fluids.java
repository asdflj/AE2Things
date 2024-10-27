package com.asdflj.ae2thing.client.icon;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.Fluid;

import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.util.NameConst;

public enum Fluids {

    Mana("fluids/mana", AE2ThingAPI.instance()
        .getMana());

    private final String name;
    private final Fluid fluid;
    public net.minecraft.util.IIcon IIcon;

    Fluids(String name, Fluid fluid) {
        this.name = name;
        this.fluid = fluid;
    }

    public String getName() {
        return this.name;
    }

    public IIcon getIcon() {
        return this.IIcon;
    }

    public final Fluid getFluid() {
        return this.fluid;
    }

    public void registerIcon(final TextureMap map) {
        this.IIcon = map.registerIcon(NameConst.RES_KEY + this.name);
        this.fluid.setStillIcon(this.IIcon);
    }
}
