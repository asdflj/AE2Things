package com.asdflj.ae2thing.coremod.hooker;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.nei.object.OrderStack;
import com.asdflj.ae2thing.network.CPacketTransferRecipe;
import com.asdflj.ae2thing.util.BRUtil;
import com.gtnewhorizon.gtnhlib.util.CoordinatePacker;

import blockrenderer6343.client.renderer.WorldSceneRenderer;
import codechicken.nei.NEIClientUtils;
import codechicken.nei.recipe.GuiRecipe;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CoreModBRHookClient {

    public static boolean isNEELoaded = false; // always is false

    public static void neiOverlay(WorldSceneRenderer renderer) {
        // 1.3.2
        sendToServer(getIngredients(renderer));
    }

    @SuppressWarnings("unchecked")
    public static void neiOverlay(Object o) {
        // 1.2.16
        try {
            Field f = o.getClass()
                .getSuperclass()
                .getDeclaredField("ingredients");
            f.setAccessible(true);
            List<ItemStack> list = (List<ItemStack>) f.get(o);
            Method m = o.getClass()
                .getSuperclass()
                .getDeclaredMethod("getMultiblockName");
            m.setAccessible(true);
            BRUtil.setMultiBlockName((String) m.invoke(o));
            sendToServer(list);
        } catch (Exception ignored) {

        }

    }

    private static void sendToServer(List<ItemStack> ingredients) {
        if (AE2ThingAPI.instance()
            .terminal()
            .isPatternTerminal()) {
            try {
                ImmutablePair<List<OrderStack<?>>, List<OrderStack<?>>> result = BRUtil.handler.handler(ingredients);
                AE2Thing.proxy.netHandler.sendToServer(
                    new CPacketTransferRecipe(
                        result.left,
                        result.right,
                        false,
                        GuiScreen.isShiftKeyDown(),
                        Constants.NEI_BR));
            } catch (Exception ignored) {}
        }
        GuiRecipe<?> currentScreen = (GuiRecipe<?>) Minecraft.getMinecraft().currentScreen;
        Minecraft.getMinecraft()
            .displayGuiScreen(currentScreen.firstGui);
    }

    private static List<ItemStack> getIngredients(WorldSceneRenderer renderer) {
        List<ItemStack> ingredients = new ArrayList<>();
        for (long renderedBlock : renderer.renderedBlocks) {
            int x = CoordinatePacker.unpackX(renderedBlock);
            int y = CoordinatePacker.unpackY(renderedBlock);
            int z = CoordinatePacker.unpackZ(renderedBlock);
            Block block = renderer.world.getBlock(x, y, z);
            if (block.equals(Blocks.air)) continue;
            int meta = renderer.world.getBlockMetadata(x, y, z);
            int qty = block.quantityDropped(renderer.world.rand);
            ArrayList<ItemStack> itemStacks = new ArrayList<>();
            if (qty != 1) {
                itemStacks.add(new ItemStack(block));
            } else {
                itemStacks = block.getDrops(renderer.world, x, y, z, meta, 0);
            }
            boolean added = false;
            for (ItemStack ingredient : ingredients) {
                if (NEIClientUtils.areStacksSameTypeWithNBT(ingredient, itemStacks.get(0))) {
                    ingredient.stackSize++;
                    added = true;
                    break;
                }
            }
            if (!added) ingredients.add(itemStacks.get(0));
        }

        return ingredients;
    }
}
