package com.asdflj.ae2thing.common.storage.backpack;

import com.glodblock.github.common.item.ItemFluidDrop;
import net.minecraft.item.ItemStack;

import com.darkona.adventurebackpack.inventory.InventoryBackpack;

import appeng.util.Platform;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public class AdventureBackpackHandler extends BaseBackpackHandler {

    private final InventoryBackpack inventory;

    public AdventureBackpackHandler(ItemStack is) {
        super(new InventoryBackpack(is));
        this.inventory = (InventoryBackpack) inv;
    }

    @Override
    public ItemStack getStackInSlot(int slotIn) {
        if(slotIn >= this.getItemSizeInventory()){
            return ItemFluidDrop.newStack(this.getStack(slotIn));
        }
        return super.getStackInSlot(slotIn);
    }

    private FluidStack getStack(int slot){
        FluidTank tank = this.getTank(slot);
        return tank == null ? null : tank.getFluid();
    }

    private FluidTank getTank(int slot){
        if(slot == this.getSizeInventory() -1 ){
            return this.inventory.getRightTank();
        }else if(slot == this.getSizeInventory() -2 ){
            return this.inventory.getLeftTank();
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    @Override
    public void setInventorySlotContents(int slotIn, ItemStack stack) {
        if(slotIn >= this.getItemSizeInventory()){
            this.getTank(slotIn).setFluid(ItemFluidDrop.getFluidStack(stack));
        }else{
            super.setInventorySlotContents(slotIn, stack);
        }
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        if(index >= this.getItemSizeInventory()){
            FluidStack fs = this.getStack(index);
            if(fs == null) return null;
            FluidStack f = fs.copy();
            f.amount -= count;
            return ItemFluidDrop.newStack(f);
        }
        return super.decrStackSize(index, count);
    }
    private boolean isSameFluid(FluidStack fs1, FluidStack fs2){
        if(fs1 == null ||  fs2 == null) return false;
        return fs1.getFluid() == fs2.getFluid();
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack is) {
        if(slot >= this.getItemSizeInventory()){
            FluidTank tank = this.getTank(slot);
            FluidStack fs =tank.getFluid();
            if(fs == null) return true;
            return fs.amount < tank.getCapacity() && isSameFluid(fs,ItemFluidDrop.getFluidStack(is));
        }
        ItemStack slotItem = inv.getStackInSlot(slot);
        if (slotItem == null) return true;
        if (!Platform.isSameItemPrecise(is, slotItem)) return false;
        return slotItem.stackSize < slotItem.getMaxStackSize();
    }

    @Override
    public int getSizeInventory() {
        return super.getSizeInventory() + 2;
    }

    private int getItemSizeInventory(){
        return super.getSizeInventory();
    }
}
