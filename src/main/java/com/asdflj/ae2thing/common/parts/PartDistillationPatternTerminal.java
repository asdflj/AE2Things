package com.asdflj.ae2thing.common.parts;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.glodblock.github.client.textures.FCPartsTexture;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.BiggerAppEngInventory;
import appeng.tile.inventory.InvOperation;

public class PartDistillationPatternTerminal extends THPart {

    private static final FCPartsTexture FRONT_BRIGHT_ICON = FCPartsTexture.PartFluidPatternTerminal_Bright;
    private static final FCPartsTexture FRONT_DARK_ICON = FCPartsTexture.PartFluidPatternTerminal_Colored;
    private static final FCPartsTexture FRONT_COLORED_ICON = FCPartsTexture.PartFluidPatternTerminal_Dark;

    protected final AppEngInternalInventory pattern = new AppEngInternalInventory(this, 2);
    protected AppEngInternalInventory crafting = new BiggerAppEngInventory(this, 1);
    protected AppEngInternalInventory output = new BiggerAppEngInventory(this, 16);

    public PartDistillationPatternTerminal(ItemStack is) {
        super(is, true);
    }

    @Override
    public void getDrops(final List<ItemStack> drops, final boolean wrenched) {
        super.getDrops(drops, wrenched);
        for (final ItemStack is : this.pattern) {
            if (is != null) {
                drops.add(is);
            }
        }
    }

    @Override
    public IInventory getInventoryByName(final String name) {
        if (name.equals("crafting")) {
            return crafting;
        } else if (name.equals("output")) {
            return output;
        } else if (name.equals("pattern")) {
            return this.pattern;
        }
        return null;
    }

    @Override
    public GuiType getGui() {
        return GuiType.DISTILLATION_PATTERN_TERMINAL;
    }

    @Override
    public FCPartsTexture getFrontBright() {
        return FRONT_BRIGHT_ICON;
    }

    @Override
    public FCPartsTexture getFrontColored() {
        return FRONT_COLORED_ICON;
    }

    @Override
    public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removedStack,
        ItemStack newStack) {
        if (inv == this.pattern && slot == 1) {
            final ItemStack is = inv.getStackInSlot(1);
            if (is != null && is.getItem() instanceof final ICraftingPatternItem craftingPatternItem) {
                final ICraftingPatternDetails details = craftingPatternItem
                    .getPatternForItem(is, this.getHost().getTile().getWorldObj());
                if (details != null) {
                    final IAEItemStack[] inItems = details.getInputs();
                    final IAEItemStack[] outItems = details.getOutputs();

                    for (int i = 0; i < this.crafting.getSizeInventory(); i++) {
                        this.crafting.setInventorySlotContents(i, null);
                    }

                    for (int i = 0; i < this.output.getSizeInventory(); i++) {
                        this.output.setInventorySlotContents(i, null);
                    }

                    Arrays.stream(inItems).filter(Objects::nonNull).findFirst().ifPresent(x->this.crafting.setInventorySlotContents(0,x.getItemStack()));
                    for (int i = 0; i < this.output.getSizeInventory() && i < outItems.length; i++) {
                        if (outItems[i] != null) {
                            final IAEItemStack item = outItems[i];
                            this.output.setInventorySlotContents(i, item == null ? null : item.getItemStack());
                        }
                    }
                }
            }
        }
        this.getHost().markForSave();
    }

    @Override
    public FCPartsTexture getFrontDark() {
        return FRONT_DARK_ICON;
    }

    @Override
    public boolean isLightSource() {
        return false;
    }

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);
        this.pattern.readFromNBT(data, "pattern");
        this.output.readFromNBT(data, "outputList");
        this.crafting.readFromNBT(data, "craftingGrid");
    }

    @Override
    public void writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);
        this.pattern.writeToNBT(data, "pattern");
        this.output.writeToNBT(data, "outputList");
        this.crafting.writeToNBT(data, "craftingGrid");
    }
}
