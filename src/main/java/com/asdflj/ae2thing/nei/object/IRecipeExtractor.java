package com.asdflj.ae2thing.nei.object;

import java.util.List;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;

public interface IRecipeExtractor {

    List<OrderStack<?>> getInputIngredients(List<PositionedStack> rawInputs);

    List<OrderStack<?>> getOutputIngredients(List<PositionedStack> rawOutputs);

    default List<OrderStack<?>> getInputIngredients(List<PositionedStack> rawInputs, IRecipeHandler recipe, int index) {
        return getInputIngredients(rawInputs);
    }

    default List<OrderStack<?>> getOutputIngredients(List<PositionedStack> rawOutputs, IRecipeHandler recipe,
        int index) {
        return getOutputIngredients(rawOutputs);
    }
}
