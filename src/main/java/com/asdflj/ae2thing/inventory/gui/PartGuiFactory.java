package com.asdflj.ae2thing.inventory.gui;

import javax.annotation.Nullable;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;

public abstract class PartGuiFactory<T> extends TileGuiFactory<T> {

    PartGuiFactory(Class<T> invClass) {
        super(invClass);
    }

    @Nullable
    @Override
    protected T getInventory(TileEntity tile, ForgeDirection face) {
        if (tile instanceof IPartHost) {
            IPart part = ((IPartHost) tile).getPart(face);
            if (invClass.isInstance(part)) {
                return invClass.cast(part);
            }
        }
        return null;
    }
}
