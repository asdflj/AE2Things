package com.asdflj.ae2thing.api.adapter.item.terminal;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.util.BaublesUtil;
import com.asdflj.ae2thing.util.ModAndClassUtil;

public interface IItemTerminal {

    default boolean supportBaubles() {
        return false;
    }

    List<Class<? extends Item>> getClasses();

    default List<TerminalItems> getTerminalItems() {
        List<TerminalItems> terminal = new ArrayList<>();
        IInventory inv;
        inv = this.getInventory();
        terminal.addAll(this.getInvTerminals(inv));
        if (ModAndClassUtil.BAUBLES && this.supportBaubles()) {
            terminal.addAll(getInvTerminals(BaublesUtil.getBaublesInv(this.player())));
        }
        return terminal;
    }

    default List<TerminalItems> getInvTerminals(IInventory inv) {
        List<TerminalItems> arr = new ArrayList<>();
        if (inv == null) return arr;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack item = inv.getStackInSlot(i);
            if (item == null || item.getItem() == null) continue;
            if (getClasses().contains(
                item.getItem()
                    .getClass())) {
                arr.add(new TerminalItems(item, item));
            }
        }
        return arr;
    }

    default EntityPlayer player() {
        Minecraft mc = Minecraft.getMinecraft();
        return mc.thePlayer;
    }

    default IInventory getInventory() {
        return this.player().inventory;
    }

}
