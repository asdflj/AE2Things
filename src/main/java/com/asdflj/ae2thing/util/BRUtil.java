package com.asdflj.ae2thing.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.asdflj.ae2thing.nei.ButtonConstants;
import com.asdflj.ae2thing.nei.NEI_TH_Config;
import com.asdflj.ae2thing.nei.object.OrderStack;

import codechicken.nei.recipe.GuiRecipe;

public class BRUtil {

    public static ItemStack paper = new ItemStack(Items.paper);

    private static String multiBlockName = "";

    public interface ITransferHandler {

        ImmutablePair<List<OrderStack<?>>, List<OrderStack<?>>> handler(List<ItemStack> ingredients);
    }

    public static ITransferHandler handler = ingredients -> {
        String defaultName = StatCollector.translateToLocal("blockrenderer6343.multiblock.structure");
        List<OrderStack<?>> in = new ArrayList<>();
        List<OrderStack<?>> out = new ArrayList<>();
        ItemStack item;
        for (int i = 0; i < ingredients.size(); i++) {
            item = ingredients.get(i);
            if (!((ModAndClassUtil.GT5 || ModAndClassUtil.GT5NH)
                && NEI_TH_Config.getConfigValue(ButtonConstants.BLOCK_RENDER)
                && GTUtil.isHatchItem(item))) {
                in.add(new OrderStack<>(item, i));
            }
        }
        try {
            ItemStack object = paper.copy();
            String name = ((GuiRecipe<?>) Minecraft.getMinecraft().currentScreen).getHandler()
                .getRecipeName();
            object.setStackDisplayName(name.equals(defaultName) ? multiBlockName : name);
            out.add(new OrderStack<>(object, 0));
        } catch (Exception ignored) {}
        return new ImmutablePair<>(in, out);
    };

    public static void setMultiBlockName(String name) {
        BRUtil.multiBlockName = name;
    }

    public static String getMultiBlockName() {
        return BRUtil.multiBlockName;
    }
}
