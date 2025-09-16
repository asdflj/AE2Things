package com.asdflj.ae2thing.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import com.asdflj.ae2thing.api.adapter.terminal.IGuiCraftAmount;

import cpw.mods.fml.common.eventhandler.Event;

public class UpdateAmountTextEvent extends Event {

    private static String amount = "";

    public UpdateAmountTextEvent() {}

    public UpdateAmountTextEvent(String amount) {
        UpdateAmountTextEvent.amount = amount;
    }

    public UpdateAmountTextEvent(int stackSize) {
        this(Integer.toString(stackSize));
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        if (amount.isEmpty()) return;
        GuiScreen gui = Minecraft.getMinecraft().currentScreen;
        if (gui instanceof IGuiCraftAmount guiCraftAmount) {
            try {
                guiCraftAmount.setAmount(Integer.parseInt(amount));
                UpdateAmountTextEvent.amount = "";
            } catch (NumberFormatException ignored) {
                UpdateAmountTextEvent.amount = "";
            }
        }
    }

    public void updateAmount() {
        this.setAmount(amount);
    }

    public static boolean needUpdateAmountText() {
        return !amount.isEmpty();
    }
}
