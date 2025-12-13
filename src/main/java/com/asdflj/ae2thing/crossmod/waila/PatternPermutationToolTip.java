package com.asdflj.ae2thing.crossmod.waila;

import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.client.gui.GuiWirelessDualInterfaceTerminal;
import com.asdflj.ae2thing.proxy.ClientProxy;

import appeng.container.slot.SlotFake;
import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.AcceptsFollowingTooltipLineHandler;
import codechicken.nei.recipe.RecipeHandlerRef;

public class PatternPermutationToolTip extends mcp.mobius.waila.handlers.nei.TooltipHandlerWaila {

    protected AcceptsFollowingTooltipLineHandler acceptsFollowingTooltipLineHandler;

    @Override
    public List<String> handleItemTooltip(GuiContainer gui, ItemStack itemstack, int x, int y,
        List<String> currenttip) {
        if (NEIClientConfig.showCycledIngredientsTooltip() && gui instanceof GuiWirelessDualInterfaceTerminal
            && gui.theSlot instanceof SlotFake slot
            && slot.getHasStack()
            && ClientProxy.getOverlayButton() != null) {
            RecipeHandlerRef ref = ClientProxy.getOverlayButton().handlerRef;
            List<PositionedStack> currentIngredients = ref.handler.getIngredientStacks(ref.recipeIndex);
            for (PositionedStack positionedStack : currentIngredients) {
                if (positionedStack.items.length > 1 && positionedStack.containsWithNBT(slot.getStack())) {
                    if (this.acceptsFollowingTooltipLineHandler == null
                        || this.acceptsFollowingTooltipLineHandler.tooltipGUID != positionedStack) {
                        this.acceptsFollowingTooltipLineHandler = AcceptsFollowingTooltipLineHandler
                            .of(positionedStack, positionedStack.getFilteredPermutations(), positionedStack.item);
                    }

                    if (this.acceptsFollowingTooltipLineHandler != null) {
                        this.acceptsFollowingTooltipLineHandler.setActiveStack(slot.getStack());
                        currenttip.add(
                            GuiDraw.TOOLTIP_HANDLER + GuiDraw.getTipLineId(this.acceptsFollowingTooltipLineHandler));
                        break;
                    }
                }
            }

            return currenttip;

        }
        return currenttip;
    }

}
