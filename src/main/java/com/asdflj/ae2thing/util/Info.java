package com.asdflj.ae2thing.util;

import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;

public class Info {

    public final DimensionalCoord a;
    public DimensionalCoord b;
    public String name;
    public AEColor color;
    public boolean link;
    public int dim;
    public int x;
    public int y;
    public int z;
    public int used;

    public Info(DimensionalCoord a, DimensionalCoord b, String name, AEColor color, boolean link, int used) {
        this.a = a;
        this.b = b;
        this.name = name;
        this.color = color;
        this.link = link;
        this.dim = a.getDimension();
        this.x = a.x;
        this.y = a.y;
        this.z = a.z;
        this.used = used;
    }

}
