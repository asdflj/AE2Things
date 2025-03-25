package com.asdflj.ae2thing.coremod.mixin.ae;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import appeng.client.gui.widgets.GuiScrollbar;

@Mixin(GuiScrollbar.class)
public interface AccessorGuiScrollbar {

    @Accessor(value = "isLatestClickOnScrollbar", remap = false)
    void setIsLatestClickOnScrollbar(boolean value);

}
