package com.asdflj.ae2thing.nei.object;

public class IllegalOrderStackID extends RuntimeException {

    public IllegalOrderStackID(int id) {
        super("Illegal type id: " + id);
    }
}
