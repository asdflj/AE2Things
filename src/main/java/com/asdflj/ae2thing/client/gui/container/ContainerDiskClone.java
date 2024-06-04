package com.asdflj.ae2thing.client.gui.container;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.common.item.BaseCellItem;
import com.asdflj.ae2thing.inventory.ItemDiskCloneInventory;

import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.container.AEBaseContainer;
import appeng.container.slot.AppEngSlot;

public class ContainerDiskClone extends AEBaseContainer {

    protected final ItemDiskCloneInventory it;

    private static class CloneSlot extends AppEngSlot {

        public CloneSlot(IInventory inv, int idx, int x, int y) {
            super(inv, idx, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return stack != null && stack.getItem() != null && stack.getItem() instanceof BaseCellItem;
        }

    }

    public ContainerDiskClone(InventoryPlayer ip, IGuiItemObject anchor) {
        super(ip, anchor);
        this.it = (ItemDiskCloneInventory) anchor;
        this.addSlotToContainer(new CloneSlot(it, 0, 80, 35));
        this.bindPlayerInventory(ip, 0, 84);
    }
}
