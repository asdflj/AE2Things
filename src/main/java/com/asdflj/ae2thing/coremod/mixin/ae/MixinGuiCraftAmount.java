package com.asdflj.ae2thing.coremod.mixin.ae;

import net.minecraft.inventory.Container;

import org.spongepowered.asm.mixin.Mixin;

import com.asdflj.ae2thing.api.adapter.terminal.IGuiCraftAmount;

import appeng.client.gui.implementations.GuiAmount;
import appeng.client.gui.implementations.GuiCraftAmount;

@Mixin(GuiCraftAmount.class)
public abstract class MixinGuiCraftAmount extends GuiAmount implements IGuiCraftAmount {

    public MixinGuiCraftAmount(Container container) {
        super(container);
    }

    @Override
    public int getAmount() {
        return super.getAmount();
    }

    @Override
    public void setAmount(int amount) {
        this.amountTextField.setText(String.valueOf(amount));
        this.amountTextField.setCursorPositionEnd();
        this.amountTextField.setSelectionPos(0);
    }
}
