package com.asdflj.ae2thing.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.asdflj.ae2thing.nei.object.OrderStack;

import codechicken.nei.recipe.GuiRecipe;

public class BRUtil {

    public static ItemStack paper = new ItemStack(Items.paper);

    public interface ITransferHandler {

        ImmutablePair<List<OrderStack<?>>, List<OrderStack<?>>> handler(List<ItemStack> ingredients);
    }

    public static ITransferHandler handler = ingredients -> {
        List<OrderStack<?>> in = new ArrayList<>();
        List<OrderStack<?>> out = new ArrayList<>();
        ItemStack item;
        for (int i = 0; i < ingredients.size(); i++) {
            item = ingredients.get(i);
            in.add(new OrderStack<>(item, i));
        }
        try {
            ItemStack object = paper.copy();
            object.setStackDisplayName(
                ((GuiRecipe<?>) Minecraft.getMinecraft().currentScreen).getHandler()
                    .getRecipeName());
            out.add(new OrderStack<>(object, 0));
        } catch (Exception ignored) {}
        return new ImmutablePair<>(in, out);
    };
}
