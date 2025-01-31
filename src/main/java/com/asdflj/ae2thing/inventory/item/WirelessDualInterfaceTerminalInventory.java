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
import com.asdflj.ae2thing.inventory.ItemPatternRefillInventory;
import com.asdflj.ae2thing.inventory.ItemPatternsInventory;
import com.asdflj.ae2thing.util.Util;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidPacket;

import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.TypeFilter;
import appeng.api.config.ViewItems;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.ConfigManager;
import appeng.util.Platform;

public class WirelessDualInterfaceTerminalInventory extends WirelessTerminal
    implements IGridHost, IPatternTerminal, IClickableInTerminal, IAEAppEngInventory {

    protected AppEngInternalInventory craftingEx;
    protected AppEngInternalInventory outputEx;
    protected AppEngInternalInventory pattern;
    protected AppEngInternalInventory upgrades;
    protected AppEngInternalInventory crafting;
    protected boolean craftingMode = false;
    protected boolean substitute = false;
    protected boolean combine = false;
    protected boolean prioritize = false;
    protected boolean inverted = false;
    protected boolean beSubstitute = false;
    protected int activePage = 0;
    private Util.DimensionalCoordSide tile;

    public WirelessDualInterfaceTerminalInventory(WirelessObject obj) {
        super(obj);
        pattern = new ItemPatternsInventory(obj.getItemStack(), this, obj.getPlayer(), obj.getSlot());
        crafting = new ItemBiggerAppEngInventory(
            obj.getItemStack(),
            Constants.CRAFTING,
            9,
            obj.getPlayer(),
            obj.getSlot(),
            this);
        craftingEx = new ItemBiggerAppEngInventory(
            obj.getItemStack(),
            Constants.CRAFTING_EX,
            32,
            obj.getPlayer(),
            obj.getSlot());
        outputEx = new ItemBiggerAppEngInventory(
            obj.getItemStack(),
            Constants.OUTPUT_EX,
            32,
            obj.getPlayer(),
            obj.getSlot());
        upgrades = new ItemPatternRefillInventory(
            obj.getItemStack(),
            Constants.UPGRADES,
            1,
            1,
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
        this.setCraftingRecipe(data.getBoolean("craftingMode"));
        if (data.hasKey("clickedInterface")) {
            NBTTagCompound tileMsg = (NBTTagCompound) data.getTag("clickedInterface");
            this.tile = Util.DimensionalCoordSide.readFromNBT(tileMsg);
        }
    }

    @Override
    public IConfigManager getConfigManager() {
        final ConfigManager out = new ConfigManager((manager, settingName, newValue) -> {
            final NBTTagCompound data = Platform.openNbtData(this.getItemStack());
            manager.writeToNBT(data);
            saveSettings();
        });
        out.registerSetting(Settings.SORT_BY, SortOrder.NAME);
        out.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
        out.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);
        out.registerSetting(Settings.TYPE_FILTER, TypeFilter.ALL);
        out.readFromNBT(
            (NBTTagCompound) Platform.openNbtData(this.getItemStack())
                .copy());
        return out;
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
            return this.craftingEx;
        }

        if (name.equals(Constants.OUTPUT_EX)) {
            return this.outputEx;
        }
        if (name.equals(Constants.CRAFTING)) {
            return this.crafting;
        }

        if (name.equals(Constants.PATTERN)) {
            return this.pattern;
        }
        if (name.equals(Constants.UPGRADES)) {
            return this.upgrades;
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
        this.fixCraftingRecipes();
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
        return this.craftingMode;
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
        for (ItemStack is : this.craftingEx) {
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

        for (int i = 0; i < this.craftingEx.getSizeInventory(); i++) {
            if (this.craftingEx.getStackInSlot(i) == null) break;
            if (items.isEmpty()) {
                this.craftingEx.setInventorySlotContents(i, fluids.get(i));
            } else {
                this.craftingEx.setInventorySlotContents(i, items.get(i));
            }
        }
    }

    @Override
    public void saveSettings() {
        writeToNBT();
    }

    @Override
    public boolean hasRefillerUpgrade() {
        return upgrades.getStackInSlot(0) != null;
    }

    @Override
    public void saveChanges() {

    }

    @Override
    public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removedStack,
                                  ItemStack newStack) {
        if (inv == this.pattern && slot == 1) {
            final ItemStack is = inv.getStackInSlot(1);

            if (is != null && is.getItem() instanceof final ICraftingPatternItem craftingPatternItem) {
                final ICraftingPatternDetails details = craftingPatternItem
                    .getPatternForItem(is, this.getActionableNode().getWorld());

                if (details != null) {
                    final IAEItemStack[] inItems = details.getInputs();
                    final IAEItemStack[] outItems = details.getOutputs();
                    this.setCraftingRecipe(details.isCraftable());
                    int inputsCount = 0;
                    int outputCount = 0;
                    for (IAEItemStack inItem : inItems) {
                        if (inItem != null) {
                            inputsCount++;
                        }
                    }
                    for (IAEItemStack outItem : outItems) {
                        if (outItem != null) {
                            outputCount++;
                        }
                    }

                    this.setSubstitution(details.canSubstitute());
                    if (newStack != null) {
                        this.setBeSubstitute(details.canBeSubstitute());
                    }
                    this.setInverted(inputsCount <= 8 && outputCount > 8);
                    this.setActivePage(0);

                    for (int i = 0; i < this.craftingEx.getSizeInventory(); i++) {
                        this.craftingEx.setInventorySlotContents(i, null);
                    }
                    for (int i = 0; i < this.crafting.getSizeInventory(); i++) {
                        this.crafting.setInventorySlotContents(i, null);
                    }

                    for (int i = 0; i < this.outputEx.getSizeInventory(); i++) {
                        this.outputEx.setInventorySlotContents(i, null);
                    }

                    for (int i = 0; i < getCraftingInternalInventory().getSizeInventory() && i < inItems.length; i++) {
                        final IAEItemStack item = inItems[i];
                        if (item != null) {
                            if (item.getItem() instanceof ItemFluidDrop && !this.isCraftingRecipe()) {
                                ItemStack packet = ItemFluidPacket
                                    .newStack(ItemFluidDrop.getFluidStack(item.getItemStack()));
                                getCraftingInternalInventory().setInventorySlotContents(i, packet);
                            } else getCraftingInternalInventory().setInventorySlotContents(i, item.getItemStack());
                        }
                    }

                    if (inverted) {
                        for (int i = 0; i < this.outputEx.getSizeInventory() && i < outItems.length; i++) {
                            final IAEItemStack item = outItems[i];
                            if (item != null) {
                                if (item.getItem() instanceof ItemFluidDrop) {
                                    ItemStack packet = ItemFluidPacket
                                        .newStack(ItemFluidDrop.getFluidStack(item.getItemStack()));
                                    this.outputEx.setInventorySlotContents(i, packet);
                                } else this.outputEx.setInventorySlotContents(i, item.getItemStack());
                            }
                        }
                    } else {
                        for (int i = 0; i < outItems.length && i < 8; i++) {
                            final IAEItemStack item = outItems[i];
                            if (item != null) {
                                if (item.getItem() instanceof ItemFluidDrop) {
                                    ItemStack packet = ItemFluidPacket
                                        .newStack(ItemFluidDrop.getFluidStack(item.getItemStack()));
                                    this.outputEx.setInventorySlotContents(i >= 4 ? 12 + i : i, packet);
                                } else this.outputEx.setInventorySlotContents(i >= 4 ? 12 + i : i, item.getItemStack());
                            }
                        }
                    }
                }
            }
        }
        if (inv == this.crafting) {
            this.fixCraftingRecipes();
        }
    }

    private AppEngInternalInventory getCraftingInternalInventory() {
        return this.isCraftingRecipe() ? this.crafting : this.craftingEx;
    }

    private void fixCraftingRecipes() {
        if (this.craftingMode) {
            for (int x = 0; x < this.crafting.getSizeInventory(); x++) {
                final ItemStack is = this.crafting.getStackInSlot(x);
                if (is != null) {
                    is.stackSize = 1;
                }
            }
        }
    }

    private void writeToNBT() {
        NBTTagCompound data = Platform.openNbtData(this.getItemStack());
        data.setBoolean("craftingMode", this.craftingMode);
        data.setBoolean("substitute", this.substitute);
        data.setBoolean("combine", this.combine);
        data.setBoolean("beSubstitute", this.beSubstitute);
        data.setBoolean("priorization", this.prioritize);
        data.setBoolean("inverted", this.inverted);
        data.setInteger("activePage", this.activePage);
        this.craftingEx.markDirty();
        this.outputEx.markDirty();
        this.upgrades.markDirty();
        this.pattern.markDirty();
        this.crafting.markDirty();
        NBTTagCompound tileMsg = new NBTTagCompound();
        if (tile != null) {
            tile.writeToNBT(tileMsg);
        }
        data.setTag("clickedInterface", tileMsg);
    }

    @Override
    public void setClickedInterface(Util.DimensionalCoordSide tile) {
        this.tile = tile;
        this.writeToNBT();
    }

    @Override
    public Util.DimensionalCoordSide getClickedInterface() {
        return this.tile;
    }
}
