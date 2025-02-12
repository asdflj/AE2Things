package com.asdflj.ae2thing.coremod.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.spongepowered.asm.mixin.Mixin;

import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.client.gui.widget.IGuiMonitor;
import com.asdflj.ae2thing.nei.AEItemOverlayState;
import com.asdflj.ae2thing.util.Ae2ReflectClient;
import com.asdflj.ae2thing.util.Util;
import com.glodblock.github.common.item.ItemFluidDrop;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IDisplayRepo;
import appeng.api.storage.data.IItemList;
import appeng.client.gui.AEBaseGui;
import appeng.client.me.ItemRepo;
import appeng.util.item.AEItemStack;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.recipe.GuiOverlayButton;
import codechicken.nei.recipe.IRecipeHandler;
import codechicken.nei.recipe.StackInfo;

@Mixin(value = IOverlayHandler.class)
public interface MixinIOverlayHandler extends IOverlayHandler {

    @Override
    default List<GuiOverlayButton.ItemOverlayState> presenceOverlay(GuiContainer firstGui, IRecipeHandler recipe,
        int recipeIndex) {
        final List<GuiOverlayButton.ItemOverlayState> itemPresenceSlots = new ArrayList<>();
        final List<PositionedStack> ingredients = recipe.getIngredientStacks(recipeIndex);
        IItemList<IAEItemStack> list = null;
        if (firstGui instanceof IGuiMonitor gm) {
            list = Ae2ReflectClient.getList(gm.getRepo());
        } else if (AE2ThingAPI.instance()
            .getTerminal()
            .contains(firstGui.getClass())) {
                IDisplayRepo repo = Util.getDisplayRepo((AEBaseGui) firstGui);
                if (repo instanceof ItemRepo) {
                    list = Ae2ReflectClient.getList((ItemRepo) repo);
                }
            }
        final List<ItemStack> invStacks = firstGui.inventorySlots.inventorySlots.stream()
            .filter(
                s -> s != null && s.getStack() != null
                    && s.getStack().stackSize > 0
                    && s.isItemValid(s.getStack())
                    && s.canTakeStack(firstGui.mc.thePlayer))
            .map(
                s -> s.getStack()
                    .copy())
            .collect(Collectors.toCollection(ArrayList::new));

        for (PositionedStack stack : ingredients) {
            Optional<ItemStack> used = invStacks.stream()
                .filter(is -> is.stackSize > 0 && stack.contains(is))
                .findAny();
            if (used.isPresent()) {
                ItemStack is = used.get();
                is.stackSize -= 1;
                itemPresenceSlots.add(new GuiOverlayButton.ItemOverlayState(stack, true));
            } else if (list != null) {
                boolean found = false;
                boolean isCraftable = false;
                FluidStack fs = StackInfo.getFluid(stack.item);
                IAEItemStack item;
                if (fs != null) {
                    item = ItemFluidDrop.newAeStack(fs);
                } else {
                    item = AEItemStack.create(stack.item);
                }
                if (list.findPrecise(item) != null) {
                    found = true;
                    isCraftable = list.findPrecise(item)
                        .isCraftable();
                } else if (fs == null) {
                    for (IAEItemStack is : list.findFuzzy(item, FuzzyMode.IGNORE_ALL)) {
                        if (is.getStackSize() > 0 && stack.contains(is.getItemStack())) {
                            found = true;
                            isCraftable = is.isCraftable();
                        }
                    }
                }
                itemPresenceSlots.add(new AEItemOverlayState(stack, found, isCraftable));
            } else {
                itemPresenceSlots.add(new GuiOverlayButton.ItemOverlayState(stack, false));
            }
        }

        return itemPresenceSlots;
    }
}
