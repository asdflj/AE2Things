package com.asdflj.ae2thing.loader;

import static com.asdflj.ae2thing.loader.ItemAndBlockHolder.BACKPACK_MANAGER;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

import cpw.mods.fml.common.registry.GameRegistry;

public class RecipeLoader implements Runnable {

    public static final RecipeLoader INSTANCE = new RecipeLoader();
    public static final ItemStack BUCKET = new ItemStack(Items.bucket, 1);

    @Override
    public void run() {
        GameRegistry.addRecipe(new ShapedOreRecipe(BACKPACK_MANAGER.stack(), "T", 'T', BUCKET));
    }
}
