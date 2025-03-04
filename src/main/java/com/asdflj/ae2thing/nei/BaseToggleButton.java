package com.asdflj.ae2thing.nei;

import codechicken.lib.config.ConfigTagParent;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.config.OptionToggleButton;

public class BaseToggleButton extends OptionToggleButton {

    private static final ConfigTagParent tag = NEIClientConfig.global.config;

    public BaseToggleButton(String name) {
        this(name, true);
    }

    public BaseToggleButton(String name, boolean defaultValue) {
        super(name, true);
        tag.getTag(name)
            .getBooleanValue(defaultValue);
    }
}
