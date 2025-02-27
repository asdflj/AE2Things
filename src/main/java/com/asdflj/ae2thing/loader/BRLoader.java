package com.asdflj.ae2thing.loader;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;

import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.api.adapter.pattern.AEPatternTerminalExTransferHandler;
import com.asdflj.ae2thing.api.adapter.pattern.AEPatternTerminalTransferHandler;
import com.asdflj.ae2thing.api.adapter.pattern.FCPatternTerminal;
import com.asdflj.ae2thing.api.adapter.pattern.IPatternTerminalAdapter;
import com.asdflj.ae2thing.api.adapter.pattern.IRecipeHandler;
import com.asdflj.ae2thing.client.gui.container.ContainerWirelessDualInterfaceTerminal;
import com.asdflj.ae2thing.inventory.IPatternTerminal;
import com.asdflj.ae2thing.nei.NEIUtils;
import com.glodblock.github.client.gui.container.ContainerFluidPatternExWireless;
import com.glodblock.github.client.gui.container.ContainerFluidPatternTerminal;
import com.glodblock.github.client.gui.container.ContainerFluidPatternTerminalEx;
import com.glodblock.github.client.gui.container.ContainerFluidPatternWireless;
import com.glodblock.github.inventory.item.IItemPatternTerminal;

import appeng.container.AEBaseContainer;
import appeng.container.implementations.ContainerPatternTerm;
import appeng.container.implementations.ContainerPatternTermEx;

public class BRLoader implements Runnable {

    @Override
    public void run() {
        AE2ThingAPI.instance()
            .terminal()
            .registerPatternTerminal(() -> ContainerPatternTerm.class)
            .registerIdentifier(Constants.NEI_BR, new AEPatternTerminalTransferHandler());
        AE2ThingAPI.instance()
            .terminal()
            .registerPatternTerminal(() -> ContainerPatternTermEx.class)
            .registerIdentifier(Constants.NEI_BR, new AEPatternTerminalExTransferHandler());

        IRecipeHandler handler = (container, inputs, outputs, identifier, adapter, message) -> {
            if (container instanceof AEBaseContainer c) {
                if (c.getTarget() instanceof IItemPatternTerminal terminal) {
                    terminal.setCraftingRecipe(false);
                    IInventory inputSlot = adapter.getInventoryByName(c, adapter.getCraftingInvName());
                    IInventory outputSlot = adapter.getInventoryByName(c, adapter.getOutputInvName());
                    for (int i = 0; i < inputSlot.getSizeInventory(); i++) {
                        inputSlot.setInventorySlotContents(i, null);
                    }
                    for (int i = 0; i < outputSlot.getSizeInventory(); i++) {
                        outputSlot.setInventorySlotContents(i, null);
                    }
                    inputs = NEIUtils.clearNull(inputs);
                    outputs = NEIUtils.clearNull(outputs);
                    adapter.transferPack(inputs, inputSlot);
                    adapter.transferPack(outputs, outputSlot);
                    c.onCraftMatrixChanged(inputSlot);
                    c.onCraftMatrixChanged(outputSlot);
                    terminal.saveSettings();
                }
            }
        };

        AE2ThingAPI.instance()
            .terminal()
            .registerPatternTerminal(
                new FCPatternTerminal(ContainerFluidPatternTerminal.class)
                    .registerIdentifier(Constants.NEI_BR, handler));
        AE2ThingAPI.instance()
            .terminal()
            .registerPatternTerminal(
                new FCPatternTerminal(ContainerFluidPatternWireless.class)
                    .registerIdentifier(Constants.NEI_BR, handler));
        AE2ThingAPI.instance()
            .terminal()
            .registerPatternTerminal(
                new FCPatternTerminal(ContainerFluidPatternTerminalEx.class)
                    .registerIdentifier(Constants.NEI_BR, handler));
        AE2ThingAPI.instance()
            .terminal()
            .registerPatternTerminal(
                new FCPatternTerminal(ContainerFluidPatternExWireless.class)
                    .registerIdentifier(Constants.NEI_BR, handler));
        AE2ThingAPI.instance()
            .terminal()
            .registerPatternTerminal(new IPatternTerminalAdapter() {

                @Override
                public Class<? extends Container> getContainer() {
                    return ContainerWirelessDualInterfaceTerminal.class;
                }

                @Override
                public boolean supportFluid() {
                    return true;
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
            .registerIdentifier(Constants.NEI_BR, (container, inputs, outputs, identifier, adapter, message) -> {
                if (container instanceof ContainerWirelessDualInterfaceTerminal ciw) {
                    IPatternTerminal pt = ciw.getContainer()
                        .getPatternTerminal();
                    pt.setCraftingRecipe(false);
                    IInventory inputSlot = pt.getInventoryByName(adapter.getCraftingInvName());
                    IInventory outputSlot = pt.getInventoryByName(adapter.getOutputInvName());
                    for (int i = 0; i < inputSlot.getSizeInventory(); i++) {
                        inputSlot.setInventorySlotContents(i, null);
                    }
                    for (int i = 0; i < outputSlot.getSizeInventory(); i++) {
                        outputSlot.setInventorySlotContents(i, null);
                    }
                    inputs = NEIUtils.clearNull(inputs);
                    outputs = NEIUtils.clearNull(outputs);
                    adapter.transferPack(inputs, inputSlot);
                    adapter.transferPack(outputs, outputSlot);
                    ciw.onCraftMatrixChanged(inputSlot);
                    ciw.onCraftMatrixChanged(outputSlot);
                    ciw.saveChanges();
                }
            });

    }
}
