package com.asdflj.ae2thing.client.gui.container;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.inventory.IPatternTerminal;

import appeng.container.slot.SlotFake;

public interface IPatternContainer {

    boolean isPatternTerminal();

    boolean hasRefillerUpgrade();

    void refillBlankPatterns(Slot slot);

    void encode();

    void encodeAndMoveToInventory();

    void encodeAllItemAndMoveToInventory();

    IPatternTerminal getPatternTerminal();

    void clear();

    void doubleStacks(boolean isShift);

    default boolean canDouble(SlotFake[] slots, int mult) {
        for (Slot s : slots) {
            ItemStack st = s.getStack();
            if (st != null) {
                long result = (long) s.getStack().stackSize * mult;
                if (result > Integer.MAX_VALUE) {
                    return false;
                }
            }
        }
        return true;
    }

    default void doubleStacksInternal(SlotFake[] slots, int mult) {
        List<SlotFake> enabledSlots = Arrays.stream(slots)
            .filter(SlotFake::isEnabled)
            .collect(Collectors.toList());
        for (final Slot s : enabledSlots) {
            ItemStack st = s.getStack();
            if (st != null) {
                st.stackSize *= mult;
            }
        }
    }
}
