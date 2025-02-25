package com.asdflj.ae2thing.api.adapter.pattern;

import java.util.HashMap;

public abstract class PatternTerminalAdapter implements IPatternTerminalAdapter {

    private final HashMap<String, ITransferPackHandler> identifiers = new HashMap<>();

    @Override
    public HashMap<String, ITransferPackHandler> getIdentifiers() {
        return identifiers;
    }
}
