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
        return a.x + "," + a.y + "," + a.z + "  " + a.getDimension();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color.toString();
    }

    public AEColor getAEColor() {
        return color;
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
            return a.x == i.a.x && a.y == i.a.y && a.z == i.a.z && a.getDimension() == i.a.getDimension();
        }
        return false;
    }

    public void setAEColor(AEColor color) {
        this.color = color;
    }
}
