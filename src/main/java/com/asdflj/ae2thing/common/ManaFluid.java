package com.asdflj.ae2thing.common;

import net.minecraftforge.fluids.Fluid;

import com.asdflj.ae2thing.util.NameConst;

public class ManaFluid extends Fluid {

    public ManaFluid() {
        super(NameConst.MANA);
    }

    @Override
    public int getColor() {
        return super.getColor();
        // return 0x9FFCFD;
    }
}
