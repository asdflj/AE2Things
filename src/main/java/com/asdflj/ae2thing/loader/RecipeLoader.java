package com.asdflj.ae2thing.loader;

import static com.asdflj.ae2thing.loader.ItemAndBlockHolder.BACKPACK_MANAGER;
import static com.asdflj.ae2thing.loader.ItemAndBlockHolder.BLOCK_FISH_BIG;
import static com.asdflj.ae2thing.loader.ItemAndBlockHolder.DISTILLATION_PATTERN_TERMINAL;
import static com.asdflj.ae2thing.loader.ItemAndBlockHolder.ITEM_CREATIVE_WATER_CELL;
import static com.asdflj.ae2thing.loader.ItemAndBlockHolder.ITEM_INFINITY_CELL;
import static com.asdflj.ae2thing.loader.ItemAndBlockHolder.ITEM_INFINITY_FLUID_CELL;
import static com.glodblock.github.loader.ItemAndBlockHolder.CELL_HOUSING;
import static thaumicenergistics.common.blocks.BlockEnum.DISTILLATION_ENCODER;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.glodblock.github.common.storage.CellType;

import cpw.mods.fml.common.registry.GameRegistry;

public class RecipeLoader implements Runnable {

    public static final RecipeLoader INSTANCE = new RecipeLoader();
    public static final ItemStack CHEST = new ItemStack(Blocks.chest, 1);
    public static final ItemStack CRAFTING_TABLE = new ItemStack(Blocks.crafting_table, 1);
    public static final ItemStack DIAMOND = new ItemStack(Items.diamond, 1);
    public static final ItemStack FISH = new ItemStack(Items.fish);
    public static final ItemStack BUCKET = new ItemStack(Items.bucket, 1);
    public static final ItemStack AE2_PROCESS_PATTERN_TERM = new ItemStack(
        GameRegistry.findItem("appliedenergistics2", "item.ItemMultiPart"),
        1,
        500);
    public static final ItemStack AE2_DIGITAL_SINGULARITY_CELL = new ItemStack(
        GameRegistry.findItem("appliedenergistics2", "item.ItemExtremeStorageCell.Singularity"),
        1);
    public static final ItemStack AE2FC_DIGITAL_SINGULARITY_CELL = com.glodblock.github.loader.ItemAndBlockHolder.SINGULARITY_CELL
        .stack();

    @Override
    public void run() {
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                BACKPACK_MANAGER.stack(),
                "TDT",
                "TCT",
                "TDT",
                'C',
                CHEST,
                'D',
                DIAMOND,
                'T',
                CRAFTING_TABLE));
        GameRegistry.addRecipe(
            new ShapedOreRecipe(ITEM_INFINITY_CELL.stack(), "CCC", "CCC", "CCC", 'C', AE2_DIGITAL_SINGULARITY_CELL));
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ITEM_INFINITY_FLUID_CELL.stack(),
                "CCC",
                "CCC",
                "CCC",
                'C',
                AE2FC_DIGITAL_SINGULARITY_CELL));
        if (ModAndClassUtil.THE) {
            final ItemStack THE_DISTILLATION_ENCODER = new ItemStack(DISTILLATION_ENCODER.getBlock(), 1);
            GameRegistry.addShapelessRecipe(
                DISTILLATION_PATTERN_TERMINAL.stack(),
                AE2_PROCESS_PATTERN_TERM,
                THE_DISTILLATION_ENCODER);
        }
        GameRegistry.addRecipe(new ShapedOreRecipe(BLOCK_FISH_BIG.stack(), "FFF", "F F", "FFF", 'F', FISH));
        GameRegistry.addRecipe(
            new ShapelessOreRecipe(ITEM_CREATIVE_WATER_CELL, CELL_HOUSING.stack(1, 1), CellType.Cell64kPart.stack(1)));
    }
}
