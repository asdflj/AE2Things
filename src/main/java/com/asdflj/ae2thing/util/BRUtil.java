package com.asdflj.ae2thing.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.nei.ButtonConstants;
import com.asdflj.ae2thing.nei.NEI_TH_Config;
import com.asdflj.ae2thing.nei.object.OrderStack;
import com.asdflj.ae2thing.network.CPacketTransferRecipe;
import com.gtnewhorizon.gtnhlib.util.CoordinatePacker;

import blockrenderer6343.client.renderer.WorldSceneRenderer;
import codechicken.nei.NEIClientUtils;
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

    public static boolean sendToServer(List<ItemStack> ingredients) {
        if (AE2ThingAPI.instance()
            .terminal()
            .isPatternTerminal()) {
            try {
                ImmutablePair<List<OrderStack<?>>, List<OrderStack<?>>> result = com.asdflj.ae2thing.util.BRUtil.handler
                    .handler(ingredients);
                AE2Thing.proxy.netHandler.sendToServer(
                    new CPacketTransferRecipe(
                        result.left,
                        result.right,
                        false,
                        GuiScreen.isShiftKeyDown(),
                        Constants.NEI_BR));
                GuiRecipe<?> currentScreen = (GuiRecipe<?>) Minecraft.getMinecraft().currentScreen;
                Minecraft.getMinecraft()
                    .displayGuiScreen(currentScreen.firstGui);
                return true;
            } catch (Exception ignored) {}
        }
        return false;
    }

    public static List<ItemStack> getIngredients(WorldSceneRenderer renderer) {
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
