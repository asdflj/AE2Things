package com.asdflj.ae2thing.common.tile;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.asdflj.ae2thing.common.item.ItemPhial;
import com.asdflj.ae2thing.inventory.IEssentiaContainer;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.common.tile.TileFluidInterface;

import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectSource;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.common.Thaumcraft;

public class TileInfusionInterface extends TileFluidInterface
    implements IAspectSource, IEssentiaContainer, IEssentiaTransport {

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

    @Override
    public boolean isConnectable(ForgeDirection side) {
        return false;
    }

    @Override
    public boolean canInputFrom(ForgeDirection side) {
        return false;
    }

    @Override
    public boolean canOutputTo(ForgeDirection side) {
        return true;
    }

    @Override
    public void setSuction(Aspect aspect, int amount) {

    }

    @Override
    public Aspect getSuctionType(ForgeDirection var1) {
        return null;
    }

    @Override
    public int getSuctionAmount(ForgeDirection side) {
        return 8;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, ForgeDirection side) {
        int stored = aspects.getAmount(aspect);
        if (stored >= amount) {
            aspects.remove(aspect, amount);
            return amount;
        }
        return 0;
    }

    @Override
    public int addEssentia(Aspect var1, int var2, ForgeDirection var3) {
        return 0;
    }

    @Override
    public Aspect getEssentiaType(ForgeDirection side) {
        Aspect wantedAspect = this.getNeighborWantedAspect(side);
        if (wantedAspect != null) {
            if (this.aspects.getAmount(wantedAspect) > 0) {
                return wantedAspect;
            }
        }
        return null;
    }

    @Override
    public int getEssentiaAmount(ForgeDirection side) {
        Aspect wantedAspect = this.getNeighborWantedAspect(side);
        if (wantedAspect != null) {
            return this.aspects.getAmount(wantedAspect);
        }

        return 0;
    }

    protected Aspect getNeighborWantedAspect(final ForgeDirection side) {
        TileEntity neighbor = this.worldObj
            .getTileEntity(this.xCoord + side.offsetX, this.yCoord + side.offsetY, this.zCoord + side.offsetZ);

        if ((neighbor instanceof IEssentiaTransport)) {
            return ((IEssentiaTransport) neighbor).getSuctionType(side.getOpposite());
        }

        return null;
    }

    @Override
    public int getMinimumSuction() {
        return 1;
    }

    @Override
    public boolean renderExtendedTube() {
        return false;
    }
}
