package com.asdflj.ae2thing.client.render;

import java.util.function.Predicate;

import net.minecraft.inventory.Slot;

import com.asdflj.ae2thing.client.gui.IGuiDrawSlot;

import appeng.api.storage.data.IAEItemStack;

public interface ISlotRender {

    Predicate<Slot> get();

    boolean drawSlot(Slot slot, IAEItemStack stack, IGuiDrawSlot draw, boolean display);

    default void drawCallback(Slot slot, IAEItemStack stack, IGuiDrawSlot draw, boolean display) {}
}
