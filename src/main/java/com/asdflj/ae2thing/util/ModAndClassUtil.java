package com.asdflj.ae2thing.util;

import cpw.mods.fml.common.Loader;

public final class ModAndClassUtil {

    public static boolean NEI = false;
    public static boolean FTR = false;
    public static boolean BACKPACK = false;
    public static boolean ADVENTURE_BACKPACK = false;
    public static boolean HODGEPODGE = false;

    @SuppressWarnings("all")
    public static void init() {

        if (Loader.isModLoaded("Forestry")) FTR = true;
        if (Loader.isModLoaded("Backpack")) BACKPACK = true;
        if (Loader.isModLoaded("adventurebackpack")) ADVENTURE_BACKPACK = true;
        if (Loader.isModLoaded("NotEnoughItems")) NEI = true;
        if (Loader.isModLoaded("hodgepodge")) HODGEPODGE = true;
    }
}
