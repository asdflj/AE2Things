package com.asdflj.ae2thing.util;

import java.lang.reflect.Method;

import com.github.vfyjxf.nee.nei.NEECraftingHandler;
import com.github.vfyjxf.nee.network.packet.PacketNEIPatternRecipe;

import codechicken.nei.recipe.IRecipeHandler;

public class NEERecipePacker {

    public static PacketNEIPatternRecipe packRecipe(IRecipeHandler recipe, int recipeIndex) {
        try {
            Method method = NEECraftingHandler.class.getDeclaredMethod("packRecipe", IRecipeHandler.class, int.class);
            method.setAccessible(true);
            Object ret = method.invoke(new NEECraftingHandler(), recipe, recipeIndex);
            if (ret instanceof PacketNEIPatternRecipe packet) {
                return packet;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
