package com.asdflj.ae2thing.common.tile;

import com.glodblock.github.common.tile.TileFluidInterface;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectSource;

public class TileInfusionInterface extends TileFluidInterface implements IAspectSource {

    @Override
    public AspectList getAspects() {
        return null;
    }

    @Override
    public void setAspects(AspectList var1) {

    }

    @Override
    public boolean doesContainerAccept(Aspect var1) {
        return false;
    }

    @Override
    public int addToContainer(Aspect var1, int var2) {
        return 0;
    }

    @Override
    public boolean takeFromContainer(Aspect var1, int var2) {
        return false;
    }

    @Override
    public boolean takeFromContainer(AspectList var1) {
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect var1, int var2) {
        return false;
    }

    @Override
    public boolean doesContainerContain(AspectList var1) {
        return false;
    }

    @Override
    public int containerContains(Aspect var1) {
        return 0;
    }
}
