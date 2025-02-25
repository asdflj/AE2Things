package com.asdflj.ae2thing.api.adapter.pattern;

import java.util.List;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;

import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.client.gui.container.ContainerWirelessDualInterfaceTerminal;
import com.asdflj.ae2thing.inventory.IPatternTerminal;
import com.asdflj.ae2thing.nei.NEIUtils;
import com.asdflj.ae2thing.nei.object.OrderStack;

public class THPatternTerminal extends PatternTerminalAdapter {

    @Override
    public Class<? extends Container> getContainer() {
        return ContainerWirelessDualInterfaceTerminal.class;
    }

    public static class Handler implements ITransferPackHandler {

        @Override
        public void transferPack(Container container, List<OrderStack<?>> inputs, List<OrderStack<?>> outputs,
            String identifier, IPatternTerminalAdapter adapter) {
            if (container instanceof ContainerWirelessDualInterfaceTerminal ciw) {
                IPatternTerminal pt = ciw.getContainer()
                    .getPatternTerminal();
                IInventory inputSlot = pt.getInventoryByName(Constants.CRAFTING_EX);
                IInventory outputSlot = pt.getInventoryByName(Constants.OUTPUT_EX);
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

        }
    }
}
