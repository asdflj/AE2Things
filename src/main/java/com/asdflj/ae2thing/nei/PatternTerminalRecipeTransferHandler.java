package com.asdflj.ae2thing.nei;

import static com.asdflj.ae2thing.nei.NEI_TH_Config.getConfigValue;
import static com.asdflj.ae2thing.proxy.ClientProxy.mouseHandlers;
import static net.minecraft.client.gui.GuiScreen.isShiftKeyDown;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.client.gui.GuiInfusionPatternTerminal;
import com.asdflj.ae2thing.client.gui.GuiWirelessDualInterfaceTerminal;
import com.asdflj.ae2thing.nei.object.OrderStack;
import com.asdflj.ae2thing.nei.recipes.FluidRecipe;
import com.asdflj.ae2thing.network.CPacketTransferRecipe;
import com.asdflj.ae2thing.proxy.ClientProxy;
import com.asdflj.ae2thing.util.GTUtil;
import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.asdflj.ae2thing.util.PHUtil;

import appeng.api.AEApi;
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
        mouseHandlers.add((event, overlayButton) -> {
            GuiScreen screen = Minecraft.getMinecraft().currentScreen;
            if (screen instanceof AEBaseGui g && overlayButton != null && GuiScreen.isShiftKeyDown()) {
                GuiOverlayButton btn = ClientProxy.getOverlayButton();
                if (btn != null && g.theSlot instanceof SlotFake slot) {
                    ItemStack slotItem = slot.getStack();
                    if (slotItem == null) return false;

                    List<PositionedStack> list = btn.handlerRef.handler.getIngredientStacks(btn.handlerRef.recipeIndex);
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
                                    shouldCraft(btn.handlerRef.handler),
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
            setSuggestion(craft, recipe, (GuiWirelessDualInterfaceTerminal) firstGui, in);
            if (ModAndClassUtil.PH && !craft) {
                in = PHUtil.transfer(in);
            }
            List<com.glodblock.github.nei.object.OrderStack<?>> out = com.glodblock.github.nei.recipes.FluidRecipe
                .getPackageOutputs(recipe, recipeIndex, !notUseOther(recipe));
            AE2Thing.proxy.netHandler
                .sendToServer(new CPacketTransferRecipe(transfer(in), transfer(out), craft, shift));
        }
    }

    private void setSuggestion(boolean craft, IRecipeHandler recipe, GuiWirelessDualInterfaceTerminal gui,
        List<com.glodblock.github.nei.object.OrderStack<?>> in) {
        String suggestion;
        if (craft) {
            com.google.common.base.Optional<ItemStack> molecular = AEApi.instance()
                .definitions()
                .blocks()
                .molecularAssembler()
                .maybeStack(1);
            if (molecular.isPresent()) {
                suggestion = Platform.getItemDisplayName(molecular.get());
            } else {
                suggestion = "";
            }
        } else if (ModAndClassUtil.GT5NH || ModAndClassUtil.GT5) {
            suggestion = GTUtil.getRecipeName(recipe, in);
        } else {
            suggestion = recipe.getRecipeName();
        }
        if (getConfigValue(ButtonConstants.DUAL_INTERFACE_TERMINAL)) {
            gui.setSearchFieldText(suggestion);
        } else {
            gui.setSearchFieldSuggestion(suggestion);
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
