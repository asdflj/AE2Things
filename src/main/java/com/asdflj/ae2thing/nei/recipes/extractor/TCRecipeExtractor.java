package com.asdflj.ae2thing.nei.recipes.extractor;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.common.item.ItemPhial;
import com.asdflj.ae2thing.nei.object.IRecipeExtractor;
import com.asdflj.ae2thing.nei.object.OrderStack;
import com.djgiannuzz.thaumcraftneiplugin.items.ItemAspect;

import codechicken.nei.PositionedStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

public class TCRecipeExtractor implements IRecipeExtractor {

    private final boolean c;

    public TCRecipeExtractor(boolean isCraft) {
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
            if (stack != null) {
                if (stack.getStack() instanceof ItemStack is && is.getItem() instanceof ItemAspect) {
                    AspectList aspectList = ItemAspect.getAspects(is);
                    for (Aspect aspect : aspectList.getAspects()) {
                        int amount = aspectList.getAmount(aspect);
                        if (amount > 0) {
                            ItemStack out = ItemPhial.newStack(aspect, is.stackSize);
                            stack.putStack(out);
                            break;
                        }
                    }
                }
                tmp.add(stack);
            }
        }
        return tmp;
    }

    @Override
    public List<OrderStack<?>> getOutputIngredients(List<PositionedStack> rawOutputs) {
        return getInputIngredients(rawOutputs);
    }
}
