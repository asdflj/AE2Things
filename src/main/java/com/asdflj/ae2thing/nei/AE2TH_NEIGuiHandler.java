package com.asdflj.ae2thing.nei;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import codechicken.nei.api.INEIGuiAdapter;

public class AE2TH_NEIGuiHandler extends INEIGuiAdapter {

    @Override
    public boolean handleDragNDrop(GuiContainer gui, int mouseX, int mouseY, ItemStack draggedStack, int button) {

        return super.handleDragNDrop(gui, mouseX, mouseY, draggedStack, button);
    }
}
