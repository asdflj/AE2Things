package com.asdflj.ae2thing.inventory.gui;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.asdflj.ae2thing.inventory.item.IItemInventory;

import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;

public abstract class ItemGuiFactory<T> implements IGuiFactory {

    protected final Class<T> invClass;

    ItemGuiFactory(Class<T> invClass) {
        this.invClass = invClass;
    }

    @Nullable
    protected T getInventory(Object inv) {
        return invClass.isInstance(inv) ? invClass.cast(inv) : null;
    }

    @Nullable
    @Override
    public Object createServerGui(EntityPlayer player, World world, int x, int y, int z, ForgeDirection face) {
        ItemStack item = getItem(player, x);
        if (item == null || !(item.getItem() instanceof IItemInventory)) {
            return null;
        }
        T inv = getInventory(((IItemInventory) item.getItem()).getInventory(item, world, x, y, z, player));
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

    private ItemStack getItem(EntityPlayer player, int x) {
        if (x == -1) {
            return player.getCurrentEquippedItem();
        } else {
            return player.inventory.getStackInSlot(x);
        }
    }

    @Nullable
    protected abstract Object createServerGui(EntityPlayer player, T inv);

    @Nullable
    @Override
    public Object createClientGui(EntityPlayer player, World world, int x, int y, int z, ForgeDirection face) {
        ItemStack item = getItem(player, x);
        if (item == null || !(item.getItem() instanceof IItemInventory)) {
            return null;
        }
        T inv = getInventory(((IItemInventory) item.getItem()).getInventory(item, world, x, y, z, player));
        if (inv == null && Minecraft.getMinecraft().currentScreen != null) {
            player.closeScreen();
        }
        return inv != null ? createClientGui(player, inv) : null;
    }

    @Nullable
    protected abstract Object createClientGui(EntityPlayer player, T inv);
}
