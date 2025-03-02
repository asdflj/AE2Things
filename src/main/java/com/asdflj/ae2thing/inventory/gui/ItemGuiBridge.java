package com.asdflj.ae2thing.inventory.gui;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.asdflj.ae2thing.util.BaublesUtil;
import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.glodblock.github.util.Util;

public abstract class ItemGuiBridge<T> implements IGuiFactory {

    protected final Class<T> invClass;

    ItemGuiBridge(Class<T> invClass) {
        this.invClass = invClass;
    }

    protected T getInventory(Object inv) {
        return invClass.isInstance(inv) ? invClass.cast(inv) : null;
    }

    @Override
    public Object createServerGui(EntityPlayer player, World world, int x, int y, int z, ForgeDirection face) {
        ImmutablePair<Util.GuiHelper.InvType, Integer> result = Util.GuiHelper.decodeInvType(x);
        ItemStack is = null;
        if (result.left == Util.GuiHelper.InvType.PLAYER_INV) {
            is = player.inventory.getStackInSlot(result.right);
        } else if (ModAndClassUtil.BAUBLES) {
            is = BaublesUtil.getBaublesInv(player)
                .getStackInSlot(result.right);
        }
        if (is == null) return null;
        T obj = getInventory(is.getItem());
        if (obj == null) return null;
        return createServerGui(player, obj, is);
    }

    @Nullable
    @Override
    public Object createClientGui(EntityPlayer player, World world, int x, int y, int z, ForgeDirection face) {
        ImmutablePair<Util.GuiHelper.InvType, Integer> result = Util.GuiHelper.decodeInvType(x);
        ItemStack is = null;
        if (result.left == Util.GuiHelper.InvType.PLAYER_INV) {
            is = player.inventory.getStackInSlot(result.right);
        } else if (ModAndClassUtil.BAUBLES) {
            is = BaublesUtil.getBaublesInv(player)
                .getStackInSlot(result.right);
        }
        if (is == null) return null;
        T obj = getInventory(is.getItem());
        if (Minecraft.getMinecraft().currentScreen != null) {
            player.closeScreen();
        }
        return obj != null ? createClientGui(player, obj, is) : null;
    }

    @Nullable
    protected abstract Object createClientGui(EntityPlayer player, T inv, ItemStack item);

    @Nullable
    protected abstract Object createServerGui(EntityPlayer player, T inv, ItemStack item);
}
