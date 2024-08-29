package com.asdflj.ae2thing.inventory.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.api.WirelessObject;
import com.asdflj.ae2thing.inventory.IPatternTerminal;
import com.asdflj.ae2thing.inventory.ItemBiggerAppEngInventory;
import com.glodblock.github.common.item.ItemFluidPacket;

import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;

public class WirelessInterfaceTerminalInventory extends WirelessTerminal
    implements IActionHost, IGridHost, IPatternTerminal {

    protected AppEngInternalInventory crafting;
    protected AppEngInternalInventory output;
    protected AppEngInternalInventory pattern;
    protected boolean craftingMode = false;
    protected boolean substitute = false;
    protected boolean combine = false;
    protected boolean prioritize = false;
    protected boolean inverted = false;
    protected boolean beSubstitute = false;
    protected int activePage = 0;

    public WirelessInterfaceTerminalInventory(WirelessObject obj) {
        super(obj);
        pattern = new ItemBiggerAppEngInventory(
            obj.getItemStack(),
            Constants.PATTERN,
            2,
            obj.getPlayer(),
            obj.getSlot());
        crafting = new ItemBiggerAppEngInventory(
            obj.getItemStack(),
            Constants.CRAFTING_EX,
            32,
            obj.getPlayer(),
            obj.getSlot());
        output = new ItemBiggerAppEngInventory(
            obj.getItemStack(),
            Constants.OUTPUT_EX,
            32,
            obj.getPlayer(),
            obj.getSlot());
        this.readFromNBT();
    }

    private void readFromNBT() {
        NBTTagCompound data = Platform.openNbtData(this.obj.getItemStack());
        this.setSubstitution(data.getBoolean("substitute"));
        this.setCombineMode(data.getBoolean("combine"));
        this.setBeSubstitute(data.getBoolean("beSubstitute"));
        this.setPrioritization(data.getBoolean("priorization"));
        this.setInverted(data.getBoolean("inverted"));
        this.setActivePage(data.getInteger("activePage"));
    }

    @Override
    public IConfigManager getConfigManager() {
        return null;
    }

    @Override
    public IGridNode getActionableNode() {
        return this.obj.getGridNode();
    }

    @Override
    public IGridNode getGridNode(ForgeDirection dir) {
        return this.obj.getGridNode();
    }

    @Override
    public AECableType getCableConnectionType(ForgeDirection dir) {
        return null;
    }

    @Override
    public void securityBreak() {

    }

    @Override
    public IInventory getInventoryByName(String name) {
        if (name.equals(Constants.CRAFTING_EX)) {
            return this.crafting;
        }

        if (name.equals(Constants.OUTPUT_EX)) {
            return this.output;
        }

        if (name.equals(Constants.PATTERN)) {
            return this.pattern;
        }

        return null;
    }

    @Override
    public void setActivePage(int value) {
        this.activePage = value;
    }

    @Override
    public int getActivePage() {
        return this.activePage;
    }

    @Override
    public boolean shouldCombine() {
        return this.combine;
    }

    @Override
    public void setCombineMode(boolean shouldCombine) {
        this.combine = shouldCombine;
    }

    @Override
    public void setPrioritization(boolean canPrioritize) {
        this.prioritize = canPrioritize;
    }

    @Override
    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    @Override
    public void setCraftingRecipe(boolean craftingMode) {
        this.craftingMode = craftingMode;
    }

    @Override
    public void setSubstitution(boolean canSubstitute) {
        this.substitute = canSubstitute;
    }

    @Override
    public void setBeSubstitute(boolean canBeSubstitute) {
        this.beSubstitute = canBeSubstitute;
    }

    @Override
    public boolean isCraftingRecipe() {
        return false;
    }

    @Override
    public boolean isInverted() {
        return this.inverted;
    }

    @Override
    public boolean canBeSubstitute() {
        return this.beSubstitute;
    }

    @Override
    public boolean isPrioritize() {
        return this.prioritize;
    }

    @Override
    public boolean isSubstitution() {
        return this.substitute;
    }

    @Override
    public void sortCraftingItems() {
        List<ItemStack> items = new ArrayList<>();
        List<ItemStack> fluids = new ArrayList<>();
        for (ItemStack is : this.crafting) {
            if (is == null) continue;
            if (is.getItem() instanceof ItemFluidPacket) {
                fluids.add(is);
            } else {
                items.add(is);
            }
        }
        if (this.prioritize) {
            fluids.addAll(items);
            items.clear();
        } else {
            items.addAll(fluids);
            fluids.clear();
        }

        for (int i = 0; i < this.crafting.getSizeInventory(); i++) {
            if (this.crafting.getStackInSlot(i) == null) break;
            if (items.isEmpty()) {
                this.crafting.setInventorySlotContents(i, fluids.get(i));
            } else {
                this.crafting.setInventorySlotContents(i, items.get(i));
            }
        }
    }

    @Override
    public void saveSettings() {
        NBTTagCompound data = Platform.openNbtData(this.getItemStack());
        data.setBoolean("substitute", this.substitute);
        data.setBoolean("combine", this.combine);
        data.setBoolean("beSubstitute", this.beSubstitute);
        data.setBoolean("priorization", this.prioritize);
        data.setBoolean("inverted", this.inverted);
        data.setInteger("activePage", this.activePage);
    }
}
