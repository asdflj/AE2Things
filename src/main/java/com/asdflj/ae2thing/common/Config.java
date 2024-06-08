package com.asdflj.ae2thing.common;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

import com.asdflj.ae2thing.AE2Thing;

import cpw.mods.fml.relauncher.FMLInjectionData;

public class Config {

    private static final Configuration Config = new Configuration(
        new File(new File((File) FMLInjectionData.data()[6], "config"), AE2Thing.MODID + ".cfg"));
    public static boolean backPackTerminalFillItemName;
    public static boolean cellLink;

    public static void run() {
        loadCategory();
        loadProperty();
    }

    private static void loadProperty() {
        cellLink = Config
            .getBoolean("Enable link cell", AE2Thing.NAME, true, "Enable link Cell,It will link every same uuid cell");
        backPackTerminalFillItemName = Config.getBoolean(
            "Enable backpack terminal fill item name",
            AE2Thing.NAME,
            false,
            "The backpack terminal will automatically fill the search bar with items under the mouse on both sides of the NEI");
        if (Config.hasChanged()) Config.save();
    }

    private static void loadCategory() {
        Config.addCustomCategoryComment(AE2Thing.NAME, "Settings for AE2Thing.");
    }
}
