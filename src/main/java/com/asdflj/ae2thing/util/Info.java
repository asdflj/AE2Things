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

    public String getPosString() {
        return a.x + "," + a.y + "," + a.z;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color.toString();
    }

    public String getName() {
        return name;
    }

    public String getChannelsUsed() {
        return String.valueOf(used);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Info i) {
            return this.a.hashCode() == i.a.hashCode();
        }
        return false;
    }
}
