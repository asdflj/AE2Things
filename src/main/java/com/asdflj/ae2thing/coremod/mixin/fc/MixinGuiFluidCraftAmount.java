package com.asdflj.ae2thing.coremod.mixin.fc;

import net.minecraft.inventory.Container;

import org.spongepowered.asm.mixin.Mixin;

import com.asdflj.ae2thing.api.adapter.terminal.IGuiCraftAmount;
import com.glodblock.github.client.gui.GuiFluidCraftAmount;
import com.glodblock.github.client.gui.base.FCGuiAmount;

@Mixin(GuiFluidCraftAmount.class)
public abstract class MixinGuiFluidCraftAmount extends FCGuiAmount implements IGuiCraftAmount {

    public MixinGuiFluidCraftAmount(Container container) {
        super(container);
    }

    @Override
    public int getAmount() {
        return super.getAmount();
    }

    @Override
    public void setAmount(int amount) {
        this.amountBox.setText(String.valueOf(amount));
        this.amountBox.setCursorPositionEnd();
        this.amountBox.setSelectionPos(0);
    }
}
