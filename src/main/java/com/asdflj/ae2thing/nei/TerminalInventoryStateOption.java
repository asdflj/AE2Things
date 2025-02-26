package com.asdflj.ae2thing.nei;

import codechicken.lib.config.ConfigTagParent;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.config.OptionToggleButton;

public class TerminalInventoryStateOption extends OptionToggleButton {

    private static final ConfigTagParent tag = NEIClientConfig.global.config;

    public TerminalInventoryStateOption() {
        super("ae2thing.state", true);
    }

    public static boolean getValue() {
        return tag.getTag("ae2thing.state")
            .getBooleanValue(true);
    }
}
