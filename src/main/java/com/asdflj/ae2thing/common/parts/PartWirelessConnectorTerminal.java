package com.asdflj.ae2thing.common.parts;

import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.inventory.WirelessConnectorTerminal;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.glodblock.github.client.textures.FCPartsTexture;

import appeng.api.networking.IGrid;
import appeng.me.GridAccessException;

public class PartWirelessConnectorTerminal extends THPart implements WirelessConnectorTerminal {

    private static final FCPartsTexture FRONT_BRIGHT_ICON = FCPartsTexture.PartFluidPatternTerminal_Bright;
    private static final FCPartsTexture FRONT_DARK_ICON = FCPartsTexture.PartFluidPatternTerminal_Colored;
    private static final FCPartsTexture FRONT_COLORED_ICON = FCPartsTexture.PartFluidPatternTerminal_Dark;

    public PartWirelessConnectorTerminal(ItemStack is) {
        super(is, true);
    }

    @Override
    public GuiType getGui() {
        return GuiType.WIRELESS_CONNECTOR_TERMINAL;
    }

    @Override
    public FCPartsTexture getFrontBright() {
        return FRONT_BRIGHT_ICON;
    }

    @Override
    public FCPartsTexture getFrontColored() {
        return FRONT_COLORED_ICON;
    }

    @Override
    public FCPartsTexture getFrontDark() {
        return FRONT_DARK_ICON;
    }

    @Override
    public boolean isLightSource() {
        return false;
    }

    @Override
    public IGrid getGrid() {
        try {
            return this.proxy.getGrid();
        } catch (GridAccessException e) {
            return null;
        }
    }
}
