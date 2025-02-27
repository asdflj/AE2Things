package com.asdflj.ae2thing.api.adapter.pattern;

import java.util.List;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;

import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.nei.NEIUtils;
import com.asdflj.ae2thing.nei.object.OrderStack;

import appeng.container.implementations.ContainerPatternTermEx;

public class AEPatternTerminalExTransferHandler implements IRecipeHandler {

    @Override
    public void transferPack(Container container, List<OrderStack<?>> inputs, List<OrderStack<?>> outputs,
        String identifier, IPatternTerminalAdapter adapter) {
        ContainerPatternTermEx c = (ContainerPatternTermEx) container;
        IInventory inputSlot = adapter.getInventoryByName(c, Constants.CRAFTING);
        IInventory outputSlot = adapter.getInventoryByName(c, Constants.OUTPUT);
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
        c.getPatternTerminal()
            .saveChanges();
    }
}
