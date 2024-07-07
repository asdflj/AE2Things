package com.asdflj.ae2thing.nei;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.asdflj.ae2thing.nei.object.OrderStack;

public class NEIUtils {

    public static List<OrderStack<?>> compress(List<OrderStack<?>> list) {
        List<OrderStack<?>> comp = new LinkedList<>();
        for (OrderStack<?> orderStack : list) {
            if (orderStack == null) continue;
            if (orderStack.getStack() instanceof FluidStack) {
                comp.add(orderStack);
                continue;
            }
            ItemStack currentStack = (ItemStack) orderStack.getStack();
            if (currentStack.stackSize == 0) continue;
            boolean find = false;
            for (OrderStack<?> storedStack : comp) {
                if (storedStack == null || !(storedStack.getStack() instanceof ItemStack firstStack)) continue;
                boolean areItemStackEqual = firstStack.isItemEqual(currentStack)
                    && ItemStack.areItemStackTagsEqual(firstStack, currentStack);
                if (areItemStackEqual
                    && (firstStack.stackSize + currentStack.stackSize) <= firstStack.getMaxStackSize()) {
                    find = true;
                    ((ItemStack) storedStack.getStack()).stackSize = firstStack.stackSize + currentStack.stackSize;
                }
            }
            if (!find) {
                comp.add(orderStack);
            }
        }
        return comp.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public static List<OrderStack<?>> clearNull(List<OrderStack<?>> list) {
        AtomicInteger i = new AtomicInteger(0);
        return list.stream()
            .filter(Objects::nonNull)
            .filter(
                orderStack -> !(orderStack.getStack() != null && orderStack.getStack() instanceof ItemStack
                    && ((ItemStack) orderStack.getStack()).stackSize == 0))
            .peek(orderStack -> orderStack.setIndex(i.getAndIncrement()))
            .collect(Collectors.toList());
    }
}
