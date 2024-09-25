package com.asdflj.ae2thing.client.gui.widget;

import com.github.bsideup.jabel.Desugar;

public interface IDraggable {

    @Desugar
    record Rectangle(int x, int y, int width, int height) {}

    boolean draggable();

    Rectangle getRectangle();

    void setRectangle(int x, int y);

    default void move(int x, int y) {
        this.setRectangle(x, y);
    }
}
