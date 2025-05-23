package com.asdflj.ae2thing.crossmod.waila;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import com.asdflj.ae2thing.proxy.ClientProxy;
import com.asdflj.ae2thing.util.Util;

import appeng.client.gui.AEBaseGui;
import appeng.container.slot.SlotFake;
import appeng.util.Platform;
import codechicken.lib.gui.GuiDraw;
import codechicken.nei.ItemsTooltipLineHandler;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.NEIClientUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.GuiOverlayButton;
import codechicken.nei.recipe.GuiRecipe;

public class PatternPermutationToolTip extends mcp.mobius.waila.handlers.nei.TooltipHandlerWaila {

    protected PermutationTooltipLineHandler permutationTooltipLineHandler;

    protected static class PermutationTooltipLineHandler extends ItemsTooltipLineHandler {

        protected PositionedStack pStack;
        protected ItemStack lastStack;
        protected int i = 0;
        protected long lastTime = System.currentTimeMillis();

        public PermutationTooltipLineHandler(PositionedStack pStack, List<ItemStack> items) {
            super(NEIClientUtils.translate("recipe.accepts"), items, false, 5);
            this.pStack = pStack;
            if (this.pStack.items.length > 0) {
                this.lastStack = this.pStack.items[0];
            } else {
                this.lastStack = this.pStack.item;
            }

        }

        public static PermutationTooltipLineHandler getInstance(PositionedStack pStack) {
            if (pStack.items.length > 0) {
                return new PermutationTooltipLineHandler(pStack, Arrays.asList(pStack.items));
            }

            return null;
        }

        public void update() {
            if (lastTime + 1000 < System.currentTimeMillis() && this.pStack.items.length > 0) {
                i++;
                if (i >= this.pStack.items.length) {
                    i = 0;
                }
                this.lastStack = this.pStack.items[i];
                this.lastTime = System.currentTimeMillis();
                GuiScreen gui = Minecraft.getMinecraft().currentScreen;
                if (NEIClientUtils.altKey() && this.lastStack != null && gui instanceof AEBaseGui g) {
                    Util.setSearchFieldText(g, Platform.getItemDisplayName(this.lastStack));
                }
            }

        }

        @Override
        protected void drawItem(int x, int y, ItemStack drawStack, String stackSize) {
            if (Platform.isSameItemPrecise(drawStack, this.lastStack)) {
                GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GuiDraw.drawRect(x - 1, y - 1, 18, 18, 0x66555555);
                GL11.glPopAttrib();
            }

            super.drawItem(x, y, drawStack, stackSize);
        }

    }

    @Override
    public List<String> handleItemTooltip(GuiContainer gui, ItemStack itemstack, int arg2, int arg3,
        List<String> currenttip) {
        if (NEIClientConfig.showCycledIngredientsTooltip() && gui.theSlot instanceof SlotFake slot
            && slot.getHasStack()
            && ClientProxy.getRecipe() != null) {
            GuiRecipe<?> recipe = ClientProxy.getRecipe();
            Optional<GuiButton> guiButton = recipe.getOverlayButtons()
                .stream()
                .findFirst();
            if (guiButton.isPresent()) {
                PositionedStack focused = null;
                ItemStack slotItem = slot.getStack();
                if (slotItem == null) return currenttip;
                GuiOverlayButton btn = (GuiOverlayButton) guiButton.get();
                List<PositionedStack> list = btn.handler.getIngredientStacks(btn.recipeIndex);
                out: for (PositionedStack stack : list) {
                    for (ItemStack item : stack.items) {
                        if (Platform.isSameItemType(slotItem, item)) {
                            focused = stack;
                            break out;
                        }
                    }

                }
                if (focused == null || focused.items.length <= 1) {
                    this.permutationTooltipLineHandler = null;
                } else if (this.permutationTooltipLineHandler == null
                    || this.permutationTooltipLineHandler.pStack != focused) {
                        this.permutationTooltipLineHandler = PermutationTooltipLineHandler.getInstance(focused);
                    }
                if (this.permutationTooltipLineHandler != null) {
                    this.permutationTooltipLineHandler.update();
                    currenttip.add(
                        currenttip.size() - 1,
                        GuiDraw.TOOLTIP_HANDLER + GuiDraw.getTipLineId(this.permutationTooltipLineHandler));
                }
            }
        }
        return currenttip;
    }

}
