package com.asdflj.ae2thing.client.gui.widget;

public interface IClickable {

    boolean mouseClicked(final int xPos, final int yPos);

    void onClick();

    int getIndex();

    default void unfocused() {

    }
}
