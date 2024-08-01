package com.asdflj.ae2thing.client.gui.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.inventory.ItemCellLinkInventory;

import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.container.AEBaseContainer;
import appeng.container.slot.AppEngSlot;
import appeng.util.Platform;

public class ContainerCellLink extends AEBaseContainer {

    protected final ItemCellLinkInventory it;
    protected final LinkSlot slot;

    public static class LinkSlot extends AppEngSlot {

        private final ItemCellLinkInventory it;

        public LinkSlot(IInventory inv, int idx, int x, int y) {
            super(inv, idx, x, y);
            this.it = (ItemCellLinkInventory) inv;
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            if (stack == null || stack.getItem() == null
                || !(stack.getItem()
                    .getClass()
                    .isInstance(
                        it.getItemStack()
                            .getItem())))
                return false;

            NBTTagCompound data = Platform.openNbtData(stack);
            return data.getString(Constants.DISKUUID)
                .equals(it.getUUID()) || data.getBoolean(Constants.IS_EMPTY)
                || data.hasNoTags();
        }

    }

    public ContainerCellLink(InventoryPlayer ip, IGuiItemObject anchor) {
        super(ip, anchor);
        this.it = (ItemCellLinkInventory) anchor;
        this.addSlotToContainer(slot = new LinkSlot(it, 0, 80, 35));
        this.lockPlayerInventorySlot(it.getInventorySlot());
        this.bindPlayerInventory(ip, 0, 84);
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        if (slot.getHasStack()) {
            if (!getPlayerInv().addItemStackToInventory(
                slot.getStack()
                    .copy())) {
                getPlayerInv().player.entityDropItem(
                    slot.getStack()
                        .copy(),
                    0);
            }
        }
        super.onContainerClosed(player);
    }
}
