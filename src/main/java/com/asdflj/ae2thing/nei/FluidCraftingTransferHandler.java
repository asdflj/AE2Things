package com.asdflj.ae2thing.nei;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.client.gui.GuiCraftingTerminal;
import com.asdflj.ae2thing.network.CPacketNEIRecipe;

import appeng.container.slot.SlotCraftingMatrix;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.util.Platform;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.recipe.IRecipeHandler;

public class FluidCraftingTransferHandler implements IOverlayHandler {

    public static final FluidCraftingTransferHandler INSTANCE = new FluidCraftingTransferHandler();

    @Override
    public void overlayRecipe(GuiContainer firstGui, IRecipeHandler recipe, int recipeIndex, boolean shift) {
        try {
            final List<PositionedStack> ingredients = recipe.getIngredientStacks(recipeIndex);
            if (firstGui instanceof GuiCraftingTerminal) {
                CPacketNEIRecipe packet = new CPacketNEIRecipe(packIngredients(firstGui, ingredients));
                AE2Thing.proxy.netHandler.sendToServer(packet);
            }
        } catch (final Exception | Error ignored) {
            // NO-OP
        }
    }

    private NBTTagCompound packIngredients(GuiContainer gui, List<PositionedStack> ingredients) throws IOException {
        final NBTTagCompound recipe = new NBTTagCompound();
        for (final PositionedStack positionedStack : ingredients) {
            final int col = (positionedStack.relx - 25) / 18;
            final int row = (positionedStack.rely - 6) / 18;
            if (positionedStack.items != null && positionedStack.items.length > 0) {
                for (final Object o : gui.inventorySlots.inventorySlots) {
                    if (o instanceof SlotCraftingMatrix || o instanceof SlotFakeCraftingMatrix) {
                        Slot slot = (Slot) o;
                        if (slot.getSlotIndex() == col + row * 3) {
                            final NBTTagList tags = new NBTTagList();
                            final List<ItemStack> list = new LinkedList<>();
                            // prefer pure crystals.
                            for (int x = 0; x < positionedStack.items.length; x++) {
                                if (Platform.isRecipePrioritized(positionedStack.items[x])) {
                                    list.add(0, positionedStack.items[x]);
                                } else {
                                    list.add(positionedStack.items[x]);
                                }
                            }
                            for (final ItemStack is : list) {
                                final NBTTagCompound tag = new NBTTagCompound();
                                is.writeToNBT(tag);
                                tags.appendTag(tag);
                            }
                            recipe.setTag("#" + slot.getSlotIndex(), tags);
                            break;
                        }
                    }
                }
            }
        }
        return recipe;
    }
}
