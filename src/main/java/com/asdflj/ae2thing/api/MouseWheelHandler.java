package com.asdflj.ae2thing.api;

import appeng.api.events.GuiScrollEvent;
import codechicken.nei.recipe.GuiOverlayButton;

@FunctionalInterface
public interface MouseWheelHandler {

    boolean handleMouseWheel(GuiScrollEvent event, GuiOverlayButton overlayButton);
}
