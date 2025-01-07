package com.asdflj.ae2thing.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import net.minecraft.item.ItemStack;

import com.glodblock.github.nei.object.OrderStack;

import reobf.proghatches.item.ItemProgrammingCircuit;
import reobf.proghatches.item.ItemProgrammingToolkit;

public class PHUtil {

    public static List<OrderStack<?>> transfer(List<OrderStack<?>> inputs) {
        AtomicBoolean circuit = new AtomicBoolean(false);
        if (!ItemProgrammingToolkit.holding()) {
            return inputs;
        }
        AtomicInteger i = new AtomicInteger(0);
        ArrayList<OrderStack<?>> spec = new ArrayList<>();
        List<OrderStack<?>> ret = inputs.stream()
            .filter(Objects::nonNull)
            .sorted(Comparator.comparingInt(OrderStack::getIndex))
            .filter(orderStack -> {
                boolean regular = !(orderStack.getStack() != null && orderStack.getStack() instanceof ItemStack
                    && ((ItemStack) orderStack.getStack()).stackSize == 0);
                if (!regular) {
                    circuit.set(true);
                    spec.add(
                        new OrderStack<>(
                            ItemProgrammingCircuit.wrap(((ItemStack) orderStack.getStack())),
                            orderStack.getIndex()));
                    return false;
                }

                return true;
            })
            .collect(Collectors.toList());

        if (!circuit.get() && ItemProgrammingToolkit.addEmptyProgCiruit()) {
            spec.add(0, new OrderStack<>(ItemProgrammingCircuit.wrap(null), 0));
        }

        spec.addAll(ret);
        spec.forEach((orderStack -> orderStack.setIndex(i.getAndIncrement())));

        return spec;
    }
}
