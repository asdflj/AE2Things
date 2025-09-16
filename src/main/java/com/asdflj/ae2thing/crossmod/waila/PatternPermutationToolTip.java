package com.asdflj.ae2thing.crossmod.waila;

import java.util.List;
import java.util.Optional;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.client.gui.GuiWirelessDualInterfaceTerminal;
import com.asdflj.ae2thing.proxy.ClientProxy;

import appeng.container.slot.SlotFake;
import appeng.util.Platform;
import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.AcceptsFollowingTooltipLineHandler;
import codechicken.nei.recipe.GuiOverlayButton;
import codechicken.nei.recipe.GuiRecipe;

public class PatternPermutationToolTip extends mcp.mobius.waila.handlers.nei.TooltipHandlerWaila {

    protected AcceptsFollowingTooltipLineHandler acceptsFollowingTooltipLineHandler;

    @Override
    public List<String> handleItemTooltip(GuiContainer gui, ItemStack itemstack, int arg2, int arg3,
        List<String> currenttip) {
        if (NEIClientConfig.showCycledIngredientsTooltip() && gui instanceof GuiWirelessDualInterfaceTerminal
            && gui.theSlot instanceof SlotFake slot
            && slot.getHasStack()
            && ClientProxy.getRecipe() != null) {
            GuiRecipe<?> recipe = ClientProxy.getRecipe();
            Optional<GuiButton> guiButton = recipe.getOverlayButtons()
                .stream()
                .findFirst();
            if (guiButton.isPresent()) {
                PositionedStack currentIngredients = null;
                ItemStack slotItem = slot.getStack();
                if (slotItem == null) return currenttip;
                GuiOverlayButton btn = (GuiOverlayButton) guiButton.get();
                List<PositionedStack> list = btn.handlerRef.handler.getIngredientStacks(btn.handlerRef.recipeIndex);
                out: for (PositionedStack stack : list) {
                    for (ItemStack item : stack.items) {
                        if (Platform.isSameItemPrecise(slotItem, item)) {
                            currentIngredients = stack;
                            break out;
                        }
                    }

                }
                if (currentIngredients != null && currentIngredients.items.length > 1
                    && currentIngredients.containsWithNBT(slot.getStack())) {
                    if (this.acceptsFollowingTooltipLineHandler == null
                        || this.acceptsFollowingTooltipLineHandler.tooltipGUID != currentIngredients) {
                        this.acceptsFollowingTooltipLineHandler = AcceptsFollowingTooltipLineHandler.of(
                            currentIngredients,
                            currentIngredients.getFilteredPermutations(),
                            currentIngredients.item);
                    }
                    if (this.acceptsFollowingTooltipLineHandler != null) {
                        this.acceptsFollowingTooltipLineHandler.setActiveStack(slotItem);
                        currenttip.add("§x" + GuiDraw.getTipLineId(this.acceptsFollowingTooltipLineHandler));
                    }
                }
            }
        }
        return currenttip;
    }

}
