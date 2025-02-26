package com.asdflj.ae2thing.nei;

import codechicken.lib.config.ConfigTagParent;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.config.OptionToggleButton;

public class HistoryOption extends OptionToggleButton {

    private static final ConfigTagParent tag = NEIClientConfig.global.config;

    public HistoryOption() {
        super("inventory.history", true);
    }

    public static boolean getValue() {
        return tag.getTag("inventory.history")
            .getBooleanValue(true);
    }
}
