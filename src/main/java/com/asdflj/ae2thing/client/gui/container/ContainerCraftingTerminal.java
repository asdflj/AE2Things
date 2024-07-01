package com.asdflj.ae2thing.client.gui.container;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;

import com.asdflj.ae2thing.client.gui.container.BaseMonitor.ItemMonitor;
import com.asdflj.ae2thing.inventory.item.PortableItemInventory;
import com.asdflj.ae2thing.util.Util;
import com.glodblock.github.common.item.ItemFluidDrop;

import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.ContainerNull;
import appeng.container.slot.SlotCraftingMatrix;
import appeng.container.slot.SlotCraftingTerm;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;

public class ContainerCraftingTerminal extends ContainerMonitor {

    private final SlotCraftingMatrix[] craftingSlots = new SlotCraftingMatrix[9];
    private final SlotCraftingTerm outputSlot;
    private final PortableItemInventory it;

    public ContainerCraftingTerminal(InventoryPlayer ip, ITerminalHost monitorable) {
        super(ip, monitorable);
        this.it = (PortableItemInventory) monitorable;
        this.lockSlot();
        final IInventory crafting = this.it.getInventoryByName("crafting");
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                this.addSlotToContainer(
                    this.craftingSlots[x
                        + y * 3] = new SlotCraftingMatrix(this, crafting, x + y * 3, 37 + x * 18, -72 + y * 18));
            }
        }
        AppEngInternalInventory output = new AppEngInternalInventory(this, 1);
        this.addSlotToContainer(
            this.outputSlot = new SlotCraftingTerm(
                this.getPlayerInv().player,
                this.getActionSource(),
                this.getPowerSource(),
                monitorable,
                crafting,
                crafting,
                output,
                131,
                -72 + 18,
                this));
        this.bindPlayerInventory(ip, 0, 0);
        this.onCraftMatrixChanged(crafting);
    }

    @Override
    void setMonitor() {
        this.monitor.setMonitor(this.host.getItemInventory());
        this.monitor.addListener();
        this.setCellInventory(this.monitor.getMonitor());
        this.setPowerSource((IEnergySource) this.host);
    }

    public IInventory getInventoryByName(String name) {
        if (name.equals("player")) {
            return this.getInventoryPlayer();
        }
        return this.it.getInventoryByName(name);
    }

    private void lockSlot() {
        this.lockPlayerInventorySlot(this.it.getInventorySlot());
        for (int s : Util.getBackpackSlot(this.getInventoryPlayer().player)) {
            this.lockPlayerInventorySlot(s);
        }

    }

    @Override
    public void onCraftMatrixChanged(final IInventory par1IInventory) {
        final ContainerNull cn = new ContainerNull();
        final InventoryCrafting ic = new InventoryCrafting(cn, 3, 3);
        for (int x = 0; x < 9; x++) {
            ic.setInventorySlotContents(x, this.craftingSlots[x].getStack());
        }
        this.outputSlot.putStack(
            CraftingManager.getInstance()
                .findMatchingRecipe(ic, this.getPlayerInv().player.worldObj));
    }

    @Override
    public IGridNode getNetworkNode() {
        return null;
    }

    @Override
    public boolean useRealItems() {
        return true;
    }

    @Override
    public ItemStack[] getViewCells() {
        return new ItemStack[0];
    }

    @Override
    public void saveChanges() {

    }

    @Override
    public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removedStack,
        ItemStack newStack) {

    }

    @Override
    protected IAEFluidStack extractFluids(IAEFluidStack ifs, Actionable mode) {
        if (ifs.getStackSize() == 0) return ifs;
        IAEItemStack extracted = this.host.getItemInventory()
            .extractItems(ItemFluidDrop.newAeStack(ifs), mode, this.getActionSource());
        return ItemFluidDrop.getAeFluidStack(extracted);
    }

    @Override
    protected IAEFluidStack injectFluids(IAEFluidStack ifs, Actionable mode) {
        IAEItemStack injected = this.host.getItemInventory()
            .injectItems(ItemFluidDrop.newAeStack(ifs), mode, this.getActionSource());
        return ItemFluidDrop.getAeFluidStack(injected);
    }

}
