package com.asdflj.ae2thing.nei.recipes;

import com.asdflj.ae2thing.nei.recipes.extractor.TCRecipeExtractor;
import com.asdflj.ae2thing.nei.recipes.extractor.VanillaRecipeExtractor;

public class DefaultExtractorLoader implements Runnable {

    @Override
    public void run() {
        FluidRecipe.addRecipeMap("smelting", new VanillaRecipeExtractor(false));
        FluidRecipe.addRecipeMap("brewing", new VanillaRecipeExtractor(false));
        FluidRecipe.addRecipeMap("crafting", new VanillaRecipeExtractor(true));
        FluidRecipe.addRecipeMap("crafting2x2", new VanillaRecipeExtractor(true));
        FluidRecipe.addRecipeMap("infusionCrafting", new TCRecipeExtractor(false));
    }
}
