package com.asdflj.ae2thing.nei.recipes.extractor;

import java.util.LinkedList;
import java.util.List;

import com.asdflj.ae2thing.nei.object.IRecipeExtractor;
import com.asdflj.ae2thing.nei.object.OrderStack;

import codechicken.nei.PositionedStack;

public class VanillaRecipeExtractor implements IRecipeExtractor {

    private final boolean c;

    public VanillaRecipeExtractor(boolean isCraft) {
        c = isCraft;
    }

    @Override
    public List<OrderStack<?>> getInputIngredients(List<PositionedStack> rawInputs) {
        List<OrderStack<?>> tmp = new LinkedList<>();
        for (int i = 0; i < rawInputs.size(); i++) {
            if (rawInputs.get(i) == null) continue;
            final int col = (rawInputs.get(i).relx - 25) / 18;
            final int row = (rawInputs.get(i).rely - 6) / 18;
            int index = col + row * 3;
            OrderStack<?> stack = OrderStack.pack(rawInputs.get(i), c ? index : i);
            if (stack != null) tmp.add(stack);
        }
        return tmp;
    }

    @Override
    public List<OrderStack<?>> getOutputIngredients(List<PositionedStack> rawOutputs) {
        return getInputIngredients(rawOutputs);
    }
}
