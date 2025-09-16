package com.asdflj.ae2thing.api.adapter.terminal.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.api.adapter.terminal.ITerminal;
import com.asdflj.ae2thing.util.BaublesUtil;
import com.asdflj.ae2thing.util.ModAndClassUtil;

public interface IItemTerminal extends ITerminal {

    default boolean supportBaubles() {
        return false;
    }

    default List<TerminalItems> getTerminalItems() {
        List<TerminalItems> terminal = new ArrayList<>(this.getMainInvTerminals());
        if (ModAndClassUtil.BAUBLES && this.supportBaubles()) {
            terminal.addAll(getBaublesInvTerminals(BaublesUtil.getBaublesInv(this.player())));
        }
        return terminal;
    }

    default List<TerminalItems> getMainInvTerminals() {
        List<TerminalItems> arr = new ArrayList<>();
        for (int i = 0; i < this.getInventory().mainInventory.length; i++) {
            ItemStack item = this.getInventory()
                .getStackInSlot(i);
            if (item == null || item.getItem() == null) continue;
            if (getClasses().contains(
                item.getItem()
                    .getClass())) {
                arr.add(new TerminalItems(item, item));
            }
        }
        return arr;
    }

    default List<TerminalItems> getBaublesInvTerminals(IInventory inv) {
        List<TerminalItems> arr = new ArrayList<>();
        if (inv == null) return arr;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack item = inv.getStackInSlot(i);
            if (item == null || item.getItem() == null) continue;
            if (getClasses().contains(
                item.getItem()
                    .getClass())) {
                NBTTagCompound data = this.newNBT();
                data.setInteger(Constants.SLOT, i);
                arr.add(new TerminalItems(item, item, data));
            }
        }
        return arr;
    }

    default EntityPlayer player() {
        Minecraft mc = Minecraft.getMinecraft();
        return mc.thePlayer;
    }

    default InventoryPlayer getInventory() {
        return this.player().inventory;
    }

    default NBTTagCompound newNBT() {
        return new NBTTagCompound();
    }
}
