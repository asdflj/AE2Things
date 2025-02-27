package com.asdflj.ae2thing.loader;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.api.adapter.pattern.FCPatternTerminal;
import com.asdflj.ae2thing.api.adapter.pattern.IPatternTerminalAdapter;
import com.asdflj.ae2thing.api.adapter.pattern.IRecipeHandler;
import com.asdflj.ae2thing.client.gui.container.ContainerInfusionPatternTerminal;
import com.asdflj.ae2thing.client.gui.container.ContainerWirelessDualInterfaceTerminal;
import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.glodblock.github.client.gui.container.ContainerFluidPatternExWireless;
import com.glodblock.github.client.gui.container.ContainerFluidPatternTerminal;
import com.glodblock.github.client.gui.container.ContainerFluidPatternTerminalEx;
import com.glodblock.github.client.gui.container.ContainerFluidPatternWireless;

import appeng.container.implementations.ContainerPatternTerm;
import appeng.container.implementations.ContainerPatternTermEx;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.util.Platform;

public class PatternTerminalMouseWheelLoader implements Runnable {

    @Override
    public void run() {
        IRecipeHandler handler = (container, inputs, outputs, identifier, adapter, message) -> {
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
                public boolean supportFluid() {
                    return true;
                }

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
            .registerIdentifier(
                Constants.NEI_MOUSE_WHEEL,
                (container, inputs, outputs, identifier, adapter, message) -> {
                    if (container instanceof ContainerWirelessDualInterfaceTerminal c) {
                        ItemStack in = (ItemStack) inputs.get(0)
                            .getStack();
                        ItemStack out = (ItemStack) outputs.get(0)
                            .getStack();
                        IInventory inv = adapter.getInventoryByName(
                            container,
                            c.getContainer()
                                .getPatternTerminal()
                                .isCraftingRecipe() ? Constants.CRAFTING : Constants.CRAFTING_EX);
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
            .registerPatternTerminal(new FCPatternTerminal(ContainerFluidPatternTerminal.class))
            .registerIdentifier(Constants.NEI_MOUSE_WHEEL, handler);
        AE2ThingAPI.instance()
            .terminal()
            .registerPatternTerminal(new FCPatternTerminal(ContainerFluidPatternWireless.class))
            .registerIdentifier(Constants.NEI_MOUSE_WHEEL, handler);
        AE2ThingAPI.instance()
            .terminal()
            .registerPatternTerminal(new FCPatternTerminal(ContainerFluidPatternTerminalEx.class))
            .registerIdentifier(Constants.NEI_MOUSE_WHEEL, handler);
        AE2ThingAPI.instance()
            .terminal()
            .registerPatternTerminal(new FCPatternTerminal(ContainerFluidPatternExWireless.class))
            .registerIdentifier(Constants.NEI_MOUSE_WHEEL, handler);

        if (ModAndClassUtil.THE) {
            AE2ThingAPI.instance()
                .terminal()
                .registerPatternTerminal(() -> ContainerInfusionPatternTerminal.class)
                .registerIdentifier(
                    Constants.NEI_MOUSE_WHEEL,
                    (container, inputs, outputs, identifier, adapter, message) -> {
                        if (container instanceof ContainerInfusionPatternTerminal ct) {
                            ct.getPatternTerminal()
                                .setCraftingRecipe(true);
                            ct.setCrafting(true);
                            IInventory outputSlot = ct.getInventoryByName(Constants.OUTPUT);
                            ItemStack in = (ItemStack) inputs.get(0)
                                .getStack();
                            ItemStack out = (ItemStack) outputs.get(0)
                                .getStack();
                            for (int i = 0; i < outputSlot.getSizeInventory(); i++) {
                                if (Platform.isSameItemPrecise(outputSlot.getStackInSlot(i), in)) {
                                    outputSlot.setInventorySlotContents(i, out);
                                }
                            }
                            ct.onCraftMatrixChanged(outputSlot);
                            ct.saveChanges();
                        }
                    });
        }
    }

}
