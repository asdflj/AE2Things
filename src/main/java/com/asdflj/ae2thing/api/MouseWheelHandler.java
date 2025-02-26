package com.asdflj.ae2thing.api;

import appeng.api.events.GuiScrollEvent;
import codechicken.nei.recipe.GuiRecipe;

@FunctionalInterface
public interface MouseWheelHandler {

    boolean handleMouseWheel(GuiScrollEvent event, GuiRecipe<?> recipe);
}
