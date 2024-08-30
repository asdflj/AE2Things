package com.asdflj.ae2thing.util;

import net.minecraft.tileentity.TileEntity;

import appeng.api.util.IInterfaceViewable;
import gregtech.api.metatileentity.BaseMetaTileEntity;

public class GTUtil {

    public static IInterfaceViewable getIInterfaceViewable(TileEntity tile) {
        if ((ModAndClassUtil.GT5 || ModAndClassUtil.GT5NH)
            && (tile instanceof BaseMetaTileEntity bmte && bmte.getMetaTileEntity() instanceof IInterfaceViewable iv)) {
            return iv;
        }
        return null;
    }
}
