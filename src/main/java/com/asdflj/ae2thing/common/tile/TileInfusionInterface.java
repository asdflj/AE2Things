package com.asdflj.ae2thing.common.tile;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import com.asdflj.ae2thing.common.item.ItemPhial;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.common.tile.TileFluidInterface;

import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectSource;
import thaumcraft.common.Thaumcraft;

public class TileInfusionInterface extends TileFluidInterface implements IAspectSource {

    private final AspectList aspects = new AspectList();

    @Override
    public AspectList getAspects() {
        return aspects;
    }

    @Override
    public void getDrops(World w, int x, int y, int z, List<ItemStack> drops) {
        super.getDrops(w, x, y, z, drops);
        for (Aspect aspect : aspects.getAspectsSorted()) {
            int stored = aspects.getAmount(aspect);
            if (stored > 0) {
                ItemStack fluidPacket = ItemFluidPacket.newStack(ItemPhial.newEssentiaStack(aspect, stored));
                drops.add(fluidPacket);
            }
        }
    }

    @Override
    public void setAspects(AspectList var1) {
        aspects.add(var1);
    }

    public ItemStack addAspects(ItemStack is) {
        if (is.getItem() instanceof ItemPhial) {
            Aspect aspect = ItemPhial.getAspect(is);
            if (aspect == null) return is;
            int size = is.stackSize;
            long stored = aspects.getAmount(aspect);
            if (stored + size > Integer.MAX_VALUE) {
                return is;
            } else {
                aspects.add(aspect, size);
                ItemStack out = is.copy();
                out.stackSize -= size;
                if (out.stackSize <= 0) {
                    return null;
                }
                return out;
            }
        }
        return is;
    }

    @Override
    public boolean doesContainerAccept(Aspect aspect) {
        return aspects.getAmount(aspect) > 0;
    }

    @Override
    public int addToContainer(Aspect aspect, int amount) {
        aspects.add(aspect, amount);
        return amount;
    }

    private void doParticalFX(final int aspectColor) {
        // Convert each color to percentage
        float red = (aspectColor & 0xFF0000) / (float) 0xFF0000;
        float green = (aspectColor & 0x00FF00) / (float) 0x00FF00;
        float blue = (aspectColor & 0x0000FF) / (float) 0x0000FF;

        // Add particles
        for (int i = 0; i < 5; i++) {
            Thaumcraft.proxy
                .blockRunes(this.worldObj, this.xCoord, this.yCoord, this.zCoord, red, green, blue, 15, -0.1F);
        }
        for (int i = 0; i < 5; i++) {
            Thaumcraft.proxy
                .blockRunes(this.worldObj, this.xCoord, this.yCoord, this.zCoord, red, green, blue, 15, 0.1F);
        }
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        int stored = aspects.getAmount(aspect);
        if (stored >= amount) {
            aspects.remove(aspect, amount);
            doParticalFX(aspect.getColor());
            return true;
        }
        return false;
    }

    @TileEvent(TileEventType.WORLD_NBT_READ)
    public void readFromNBTEvent(NBTTagCompound data) {
        this.aspects.readFromNBT(data);
    }

    @TileEvent(TileEventType.WORLD_NBT_WRITE)
    public NBTTagCompound writeToNBTEvent(NBTTagCompound data) {
        this.aspects.writeToNBT(data);
        return data;
    }

    @Override
    public boolean takeFromContainer(AspectList var1) {
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect var1, int var2) {
        return this.aspects.getAmount(var1) > 0;
    }

    @Override
    public boolean doesContainerContain(AspectList var1) {
        return true;
    }

    @Override
    public int containerContains(Aspect var1) {
        return this.aspects.getAmount(var1);
    }
}
