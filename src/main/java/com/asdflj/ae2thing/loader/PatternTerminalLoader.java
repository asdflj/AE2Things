package com.asdflj.ae2thing.loader;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;

import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.api.adapter.pattern.IPatternTerminalAdapter;
import com.asdflj.ae2thing.client.gui.container.ContainerInfusionPatternTerminal;
import com.asdflj.ae2thing.client.gui.container.ContainerWirelessDualInterfaceTerminal;
import com.asdflj.ae2thing.inventory.IPatternTerminal;
import com.asdflj.ae2thing.nei.NEIUtils;
import com.asdflj.ae2thing.util.ModAndClassUtil;

public class PatternTerminalLoader implements Runnable {

    @Override
    public void run() {
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
            })
            .registerIdentifier(Constants.NEI_DEFAULT, (container, inputs, outputs, identifier, adapter, message) -> {
                if (container instanceof ContainerWirelessDualInterfaceTerminal ciw) {
                    boolean combine = ciw.combine;
                    ciw.setCraftingMode(message.isCraft);
                    ciw.setCrafting(message.isCraft);
                    IPatternTerminal pt = ciw.getContainer()
                        .getPatternTerminal();
                    IInventory inputSlot = pt
                        .getInventoryByName(message.isCraft ? Constants.CRAFTING : Constants.CRAFTING_EX);
                    IInventory outputSlot = pt.getInventoryByName(Constants.OUTPUT_EX);
                    for (int i = 0; i < inputSlot.getSizeInventory(); i++) {
                        inputSlot.setInventorySlotContents(i, null);
                    }
                    for (int i = 0; i < outputSlot.getSizeInventory(); i++) {
                        outputSlot.setInventorySlotContents(i, null);
                    }
                    if (!message.isCraft) {
                        if (combine) {
                            inputs = NEIUtils.compress(inputs);
                            outputs = NEIUtils.compress(outputs);
                        }
                        inputs = NEIUtils.clearNull(inputs);
                        outputs = NEIUtils.clearNull(outputs);
                    }
                    adapter.transferPack(inputs, inputSlot);
                    adapter.transferPack(outputs, outputSlot);
                    ciw.onCraftMatrixChanged(inputSlot);
                    ciw.onCraftMatrixChanged(outputSlot);
                    ciw.saveChanges();
                }
            });
        if (ModAndClassUtil.THE) {
            AE2ThingAPI.instance()
                .terminal()
                .registerPatternTerminal(() -> ContainerInfusionPatternTerminal.class)
                .registerIdentifier(
                    Constants.NEI_DEFAULT,
                    (container, inputs, outputs, identifier, adapter, message) -> {
                        if (container instanceof ContainerInfusionPatternTerminal ct) {
                            // pattern terminal only
                            boolean combine = ct.combine;
                            ct.getPatternTerminal()
                                .setCraftingRecipe(true);
                            ct.setCrafting(true);
                            IInventory inputSlot = ct.getInventoryByName(Constants.CRAFTING);
                            IInventory outputSlot = ct.getInventoryByName(Constants.OUTPUT);
                            for (int i = 0; i < inputSlot.getSizeInventory(); i++) {
                                inputSlot.setInventorySlotContents(i, null);
                            }
                            for (int i = 0; i < outputSlot.getSizeInventory(); i++) {
                                outputSlot.setInventorySlotContents(i, null);
                            }
                            if (combine) {
                                inputs = NEIUtils.compress(inputs);
                                outputs = NEIUtils.compress(outputs);
                            }
                            inputs = NEIUtils.clearNull(inputs);
                            outputs = NEIUtils.clearNull(outputs);
                            adapter.transferPack(inputs, inputSlot);
                            adapter.transferPack(outputs, outputSlot);
                            ct.onCraftMatrixChanged(inputSlot);
                            ct.onCraftMatrixChanged(outputSlot);
                            ct.saveChanges();
                        }
                    });
        }
    }
}
