package com.asdflj.ae2thing.loader;

import static net.minecraft.client.gui.GuiScreen.isShiftKeyDown;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.api.MouseWheelHandler;
import com.asdflj.ae2thing.api.adapter.pattern.IPatternTerminalAdapter;
import com.asdflj.ae2thing.api.adapter.pattern.ITransferPackHandler;
import com.asdflj.ae2thing.client.gui.container.ContainerWirelessDualInterfaceTerminal;
import com.asdflj.ae2thing.nei.object.OrderStack;
import com.asdflj.ae2thing.network.CPacketTransferRecipe;
import com.glodblock.github.client.gui.container.ContainerFluidPatternExWireless;
import com.glodblock.github.client.gui.container.ContainerFluidPatternTerminal;
import com.glodblock.github.client.gui.container.ContainerFluidPatternTerminalEx;
import com.glodblock.github.client.gui.container.ContainerFluidPatternWireless;

import appeng.client.gui.AEBaseGui;
import appeng.container.implementations.ContainerPatternTerm;
import appeng.container.implementations.ContainerPatternTermEx;
import appeng.container.slot.SlotFake;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.util.Platform;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.GuiOverlayButton;

public class PatternTerminalMouseWheelLoader implements Runnable {

    public static List<MouseWheelHandler> handlers = new ArrayList<>();

    @Override
    public void run() {
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
                                new CPacketTransferRecipe(in, out, isShiftKeyDown(), false, Constants.NEI_MOUSE_WHEEL));
                            return true;
                        }
                    }
                }
            }
            return false;
        });
        ITransferPackHandler handler = (container, inputs, outputs, identifier, adapter) -> {
            if (container instanceof IAEAppEngInventory inventory) {
                ItemStack in = (ItemStack) inputs.get(0)
                    .getStack();
                ItemStack out = (ItemStack) outputs.get(0)
                    .getStack();
                IInventory inv = adapter.getInventoryByName(container, adapter.getCraftingInvName());
                for (int i = 0; i < inv.getSizeInventory(); i++) {
                    if (Platform.isSameItemPrecise(inv.getStackInSlot(i), in)) {
                        inv.setInventorySlotContents(i, out);
                    }
                }
                container.onCraftMatrixChanged(inv);
                inventory.saveChanges();
            }
        };

        AE2ThingAPI.instance()
            .terminal()
            .registerPatternTerminal(new IPatternTerminalAdapter() {

                @Override
                public Class<? extends Container> getContainer() {
                    return ContainerWirelessDualInterfaceTerminal.class;
                }

                @Override
                public String getOutputInvName() {
                    return Constants.OUTPUT_EX;
                }

                @Override
                public String getCraftingInvName() {
                    return Constants.CRAFTING_EX;
                }
            })
            .registerIdentifier(Constants.NEI_MOUSE_WHEEL, (container, inputs, outputs, identifier, adapter) -> {
                if (container instanceof ContainerWirelessDualInterfaceTerminal c) {
                    ItemStack in = (ItemStack) inputs.get(0)
                        .getStack();
                    ItemStack out = (ItemStack) outputs.get(0)
                        .getStack();
                    IInventory inv = adapter
                        .getInventoryByName(container, c.isCraftingMode() ? Constants.CRAFTING : Constants.CRAFTING_EX);
                    for (int i = 0; i < inv.getSizeInventory(); i++) {
                        if (Platform.isSameItemPrecise(inv.getStackInSlot(i), in)) {
                            inv.setInventorySlotContents(i, out);
                        }
                    }
                    container.onCraftMatrixChanged(inv);
                    c.saveChanges();
                }
            });
        AE2ThingAPI.instance()
            .terminal()
            .registerPatternTerminal(() -> ContainerPatternTerm.class)
            .registerIdentifier(Constants.NEI_MOUSE_WHEEL, handler);
        AE2ThingAPI.instance()
            .terminal()
            .registerPatternTerminal(() -> ContainerPatternTermEx.class)
            .registerIdentifier(Constants.NEI_MOUSE_WHEEL, handler);
        AE2ThingAPI.instance()
            .terminal()
            .registerPatternTerminal(() -> ContainerFluidPatternTerminal.class)
            .registerIdentifier(Constants.NEI_MOUSE_WHEEL, handler);
        AE2ThingAPI.instance()
            .terminal()
            .registerPatternTerminal(() -> ContainerFluidPatternWireless.class)
            .registerIdentifier(Constants.NEI_MOUSE_WHEEL, handler);
        AE2ThingAPI.instance()
            .terminal()
            .registerPatternTerminal(() -> ContainerFluidPatternTerminalEx.class)
            .registerIdentifier(Constants.NEI_MOUSE_WHEEL, handler);
        AE2ThingAPI.instance()
            .terminal()
            .registerPatternTerminal(() -> ContainerFluidPatternExWireless.class)
            .registerIdentifier(Constants.NEI_MOUSE_WHEEL, handler);
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
}
