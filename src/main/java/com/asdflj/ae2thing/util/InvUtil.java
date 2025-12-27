package com.asdflj.ae2thing.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import org.apache.commons.lang3.tuple.ImmutablePair;

public class InvUtil {

    public interface IInv {

        IInventory getInventory(EntityPlayer player);

    }

    public static List<IInv> INVENTORY = new ArrayList<>();
    private final EntityPlayer player;

    public InvUtil(EntityPlayer player) {
        this.player = player;
    }

    public boolean contains(Predicate<ItemStack> item) {
        for (IInv obj : INVENTORY) {
            IInventory inv = obj.getInventory(player);
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (item.test(stack)) {
                    return true;
                }
            }
        }
        return false;
    }

    public ImmutablePair<Integer, ItemStack> find(Predicate<ItemStack> item) {
        for (IInv obj : INVENTORY) {
            IInventory inv = obj.getInventory(player);
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (item.test(stack)) {
                    return new ImmutablePair<>(i, stack);
                }
            }
        }
        return null;
    }

    public static ImmutablePair<Integer, ItemStack> find(EntityPlayer player, Predicate<ItemStack> item) {
        return new InvUtil(player).find(item);
    }

    public List<ItemStack> matcher(Predicate<ItemStack> item) {
        List<ItemStack> items = new ArrayList<>();
        for (IInv obj : INVENTORY) {
            IInventory inv = obj.getInventory(player);
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (item.test(stack)) {
                    items.add(stack);
                }
            }
        }
        return items;
    }

    public static List<ItemStack> matcher(EntityPlayer player, Predicate<ItemStack> item) {
        return new InvUtil(player).matcher(item);
    }

    public static boolean contains(EntityPlayer player, Predicate<ItemStack> item) {
        return new InvUtil(player).contains(item);
    }
}
