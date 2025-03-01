package com.asdflj.ae2thing.inventory.gui;

public abstract class ItemGuiBridge<T> implements IGuiFactory {

    protected final Class<T> invClass;

    ItemGuiBridge(Class<T> invClass) {
        this.invClass = invClass;
    }

    @javax.annotation.Nullable
    protected T getInventory(Object inv) {
        return invClass.isInstance(inv) ? invClass.cast(inv) : null;
    }

}
