package com.asdflj.ae2thing.api.adapter.pattern;

import java.util.HashMap;
import java.util.List;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.nei.object.OrderStack;
import com.asdflj.ae2thing.network.CPacketTransferRecipe;
import com.glodblock.github.common.item.ItemFluidPacket;

import appeng.core.AELog;
import appeng.helpers.IContainerCraftingPacket;

public interface IPatternTerminalAdapter {

    HashMap<Class<? extends Container>, HashMap<String, IRecipeHandler>> map = new HashMap<>();

    default boolean supportFluid() {
        return false;
    }

    Class<? extends Container> getContainer();

    default void transferPack(List<OrderStack<?>> packs, IInventory inv) {
        for (OrderStack<?> stack : packs) {
            if (stack != null) {
                int index = stack.getIndex();
                ItemStack stack1;
                if (stack.getStack() instanceof ItemStack) {
                    stack1 = ((ItemStack) stack.getStack()).copy();
                } else if (supportFluid() && stack.getStack() instanceof FluidStack) {
                    stack1 = ItemFluidPacket.newStack((FluidStack) stack.getStack());
                } else {
                    AELog.warn(new UnsupportedOperationException("Trying to get an unsupported item!"));
                    continue;
                }
                if (index < inv.getSizeInventory()) inv.setInventorySlotContents(index, stack1);
            }
        }
    }

    default IInventory getInventoryByName(Container container, String name) {
        if (container instanceof IContainerCraftingPacket c) {
            return c.getInventoryByName(name);
        }
        return null;
    }

    default String getCraftingInvName() {
        return Constants.CRAFTING;
    }

    default String getOutputInvName() {
        return Constants.OUTPUT;
    }

    default void transfer(Container container, List<OrderStack<?>> inputs, List<OrderStack<?>> outputs,
        String identifier, CPacketTransferRecipe message) {
        IRecipeHandler handler = getIdentifiers().get(identifier);
        if (handler == null) return;
        handler.transferPack(container, inputs, outputs, identifier, this, message);
    }

    default IPatternTerminalAdapter registerIdentifier(String identifier, IRecipeHandler transferPack) {
        this.getIdentifiers()
            .put(identifier, transferPack);
        return this;
    }

    default HashMap<String, IRecipeHandler> getIdentifiers() {
        HashMap<String, IRecipeHandler> m = map.getOrDefault(getContainer(), new HashMap<>());
        map.put(getContainer(), m);
        return m;
    }

}
