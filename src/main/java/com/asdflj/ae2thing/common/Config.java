package com.asdflj.ae2thing.common;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

import com.asdflj.ae2thing.AE2Thing;

import cpw.mods.fml.relauncher.FMLInjectionData;

public class Config {

    private static final Configuration Config = new Configuration(
        new File(new File((File) FMLInjectionData.data()[6], "config"), AE2Thing.MODID + ".cfg"));
    public static boolean cellLink;
    public static int magnetRange;
    public static boolean updateViewThread = true;
    public static boolean wirelessConnectorTerminalColorSelection = true;
    public static boolean wirelessConnectorTerminalInfinityConnectionRange = true;
    public static int exIOPortTransferContentsRate;
    public static boolean backpackTerminalAddTicSupport = false;
    public static int craftingHistorySize = 200;

    public static void run() {
        loadCategory();
        loadProperty();
    }

    private static void loadProperty() {
        cellLink = Config
            .getBoolean("Enable link cell", AE2Thing.NAME, true, "Enable link Cell,It will link every same uuid cell");
        magnetRange = Config
            .getInt("Backpack terminal magnet range", AE2Thing.NAME, 32, 8, 64, "Set backpack terminal magnet range");
        updateViewThread = Config
            .getBoolean("Terminal updateView thread", AE2Thing.NAME, true, "Terminal create update view thread");
        wirelessConnectorTerminalColorSelection = Config.getBoolean(
            "Wireless connector terminal color selection",
            AE2Thing.NAME,
            true,
            "Wireless connector terminal enable color selection");
        wirelessConnectorTerminalInfinityConnectionRange = Config.getBoolean(
            "Wireless connector terminal infinity connection range",
            AE2Thing.NAME,
            true,
            "Wireless connector terminal infinity connection range");
        exIOPortTransferContentsRate = Config.getInt(
            "Ex IO Port Transfer Rate",
            AE2Thing.NAME,
            256,
            256,
            Integer.MAX_VALUE,
            "Set Ex IO Port Transfer Rate. Base transfer quantity = 256. Make sure you have enough power to transfer stack.");
        backpackTerminalAddTicSupport = Config
            .getBoolean("Tic", AE2Thing.NAME, false, "Let Backpack Terminal can forge tic tool");
        craftingHistorySize = Config
            .getInt("crafting history size", AE2Thing.NAME, 200, 100, 300, "crafting history size");
        if (Config.hasChanged()) Config.save();
    }

    private static void loadCategory() {
        Config.addCustomCategoryComment(AE2Thing.NAME, "Settings for AE2Thing.");
    }
}
