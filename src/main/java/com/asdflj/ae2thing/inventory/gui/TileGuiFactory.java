package com.asdflj.ae2thing.inventory.gui;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;

public abstract class TileGuiFactory<T> implements IGuiFactory {

    protected final Class<T> invClass;

    TileGuiFactory(Class<T> invClass) {
        this.invClass = invClass;
    }

    @Nullable
    protected T getInventory(TileEntity tile, ForgeDirection face) {
        return invClass.isInstance(tile) ? invClass.cast(tile) : null;
    }

    @Nullable
    @Override
    public Object createServerGui(EntityPlayer player, World world, int x, int y, int z, ForgeDirection face) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile == null) {
            return null;
        }
        T inv = getInventory(tile, face);
        if (inv == null) {
            return null;
        }
        Object gui = createServerGui(player, inv);
        if (gui instanceof AEBaseContainer) {
            ContainerOpenContext ctx = new ContainerOpenContext(inv);
            ctx.setWorld(world);
            ctx.setX(x);
            ctx.setY(y);
            ctx.setZ(z);
            ctx.setSide(face);
            ((AEBaseContainer) gui).setOpenContext(ctx);
        }
        return gui;
    }

    @Nullable
    protected abstract Object createServerGui(EntityPlayer player, T inv);

    @Nullable
    @Override
    public Object createClientGui(EntityPlayer player, World world, int x, int y, int z, ForgeDirection face) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile == null) {
            return null;
        }
        T inv = getInventory(tile, face);
        return inv != null ? createClientGui(player, inv) : null;
    }

    @Nullable
    protected abstract Object createClientGui(EntityPlayer player, T inv);
}
