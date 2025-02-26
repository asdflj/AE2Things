package com.asdflj.ae2thing.nei;

import static net.minecraft.client.gui.GuiScreen.isShiftKeyDown;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.api.MouseWheelHandler;
import com.asdflj.ae2thing.client.gui.GuiInfusionPatternTerminal;
import com.asdflj.ae2thing.client.gui.GuiWirelessDualInterfaceTerminal;
import com.asdflj.ae2thing.nei.object.OrderStack;
import com.asdflj.ae2thing.nei.recipes.FluidRecipe;
import com.asdflj.ae2thing.network.CPacketTransferRecipe;
import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.asdflj.ae2thing.util.PHUtil;

import appeng.client.gui.AEBaseGui;
import appeng.container.slot.SlotFake;
import appeng.util.Platform;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.recipe.GuiOverlayButton;
import codechicken.nei.recipe.IRecipeHandler;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class PatternTerminalRecipeTransferHandler implements IOverlayHandler {

    public static final PatternTerminalRecipeTransferHandler INSTANCE = new PatternTerminalRecipeTransferHandler();

    public static final HashSet<String> notOtherSet = new HashSet<>();
    public static final HashSet<String> craftSet = new HashSet<>();
    public static List<MouseWheelHandler> handlers = new ArrayList<>();
    static {
        notOtherSet.add("smelting");
        notOtherSet.add("brewing");
        craftSet.add("crafting");
        craftSet.add("crafting2x2");
    }

    private static ItemStack findSameItem(ItemStack[] items, ItemStack item, Constants.MouseWheel wheel) {
        for (int i = 0; i < items.length; i++) {
            if (Platform.isSameItemPrecise(item, items[i])) {
                int index = i + wheel.direction;
                return items[index < 0 ? items.length - 1 : index % items.length];
            }
        }
        return null;
    }

    public PatternTerminalRecipeTransferHandler() {
        handlers.add((event, recipe) -> {
            GuiScreen screen = Minecraft.getMinecraft().currentScreen;
            if (screen instanceof AEBaseGui g && recipe != null && GuiScreen.isShiftKeyDown()) {
                Optional<GuiButton> guiButton = recipe.getOverlayButtons()
                    .stream()
                    .findFirst();
                if (guiButton.isPresent() && g.theSlot instanceof SlotFake slot) {
                    ItemStack slotItem = slot.getStack();
                    if (slotItem == null) return false;
                    GuiOverlayButton btn = (GuiOverlayButton) guiButton.get();
                    List<PositionedStack> list = btn.handler.getIngredientStacks(btn.recipeIndex);
                    for (PositionedStack stack : list) {
                        ItemStack result = findSameItem(
                            stack.items,
                            slotItem,
                            event.scrollAmount == -1 ? Constants.MouseWheel.NEXT : Constants.MouseWheel.PREVIEW);
                        if (result != null) {
                            List<OrderStack<?>> in = new ArrayList<>();
                            List<OrderStack<?>> out = new ArrayList<>();
                            in.add(new OrderStack<>(slotItem, 0));
                            out.add(new OrderStack<>(result, 0));
                            AE2Thing.proxy.netHandler.sendToServer(
                                new CPacketTransferRecipe(
                                    in,
                                    out,
                                    shouldCraft(btn.handler),
                                    isShiftKeyDown(),
                                    Constants.NEI_MOUSE_WHEEL));
                            return true;
                        }
                    }
                }
            }
            return false;
        });
    }

    @Override
    public void overlayRecipe(GuiContainer firstGui, IRecipeHandler recipe, int recipeIndex, boolean shift) {
        if (firstGui instanceof GuiInfusionPatternTerminal) {
            List<OrderStack<?>> in = FluidRecipe.getPackageInputs(recipe, recipeIndex, false);
            List<OrderStack<?>> out = FluidRecipe.getPackageOutputs(recipe, recipeIndex, false);
            AE2Thing.proxy.netHandler.sendToServer(new CPacketTransferRecipe(out, in, true, shift));
        } else if (firstGui instanceof GuiWirelessDualInterfaceTerminal) {
            boolean priority = ((GuiWirelessDualInterfaceTerminal) firstGui).container.prioritize;
            boolean craft = shouldCraft(recipe);
            List<com.glodblock.github.nei.object.OrderStack<?>> in;
            in = com.glodblock.github.nei.recipes.FluidRecipe.getPackageInputs(recipe, recipeIndex, !craft && priority);
            if (ModAndClassUtil.PH && !craft) {
                in = PHUtil.transfer(in);
            }
            List<com.glodblock.github.nei.object.OrderStack<?>> out = com.glodblock.github.nei.recipes.FluidRecipe
                .getPackageOutputs(recipe, recipeIndex, !notUseOther(recipe));
            AE2Thing.proxy.netHandler
                .sendToServer(new CPacketTransferRecipe(transfer(in), transfer(out), craft, shift));
        }
    }

    private boolean notUseOther(IRecipeHandler recipeHandler) {
        TemplateRecipeHandler tRecipe = (TemplateRecipeHandler) recipeHandler;
        return notOtherSet.contains(tRecipe.getOverlayIdentifier());
    }

    private static List<OrderStack<?>> transfer(List<com.glodblock.github.nei.object.OrderStack<?>> input) {
        List<OrderStack<?>> out = new ArrayList<>();
        for (com.glodblock.github.nei.object.OrderStack<?> stack : input) {
            out.add(new OrderStack<>(stack.getStack(), stack.getIndex()));
        }
        return out;
    }

    private boolean shouldCraft(IRecipeHandler recipeHandler) {
        TemplateRecipeHandler tRecipe = (TemplateRecipeHandler) recipeHandler;
        return craftSet.contains(tRecipe.getOverlayIdentifier());
    }

}
