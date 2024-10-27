package com.asdflj.ae2thing.common.fluids;

import net.minecraftforge.fluids.Fluid;

import com.asdflj.ae2thing.util.NameConst;

public class Mana extends Fluid {

    public Mana() {
        super(NameConst.MANA);
        this.setDensity(10000);
        this.setViscosity(10000);
        this.setTemperature(300);
    }

}
