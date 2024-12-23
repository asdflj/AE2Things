package com.asdflj.ae2thing.client.gui.container.slot;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.common.Config;
import com.asdflj.ae2thing.util.Ae2Reflect;
import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.asdflj.ae2thing.util.TicUtil;

import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IStorageMonitorable;
import appeng.container.slot.SlotCraftingTerm;
import appeng.helpers.IContainerCraftingPacket;
import appeng.helpers.InventoryAction;
import appeng.util.Platform;
import appeng.util.inv.AdaptorPlayerHand;

public class SlotTicCraftingTerm extends SlotCraftingTerm {

    private final IInventory craftInv;

    public SlotTicCraftingTerm(EntityPlayer player, BaseActionSource mySrc, IEnergySource energySrc,
        IStorageMonitorable storage, IInventory cMatrix, IInventory secondMatrix, IInventory output, int x, int y,
        IContainerCraftingPacket ccp) {
        super(player, mySrc, energySrc, storage, cMatrix, secondMatrix, output, x, y, ccp);
        this.craftInv = secondMatrix;
    }

    public void doClick(final InventoryAction action, final EntityPlayer who) {
        if (ModAndClassUtil.TIC && Config.backpackTerminalAddTicSupport) {
            if (this.getStack() == null) {
                return;
            }
            if (Platform.isClient()) {
                return;
            }
            for (int x = 0; x < 9; x++) {
                if (TicUtil.isTool(this.craftInv.getStackInSlot(x))) {
                    ItemStack tool = this.craftInv.getStackInSlot(x);

                    List<ItemStack> items = new ArrayList<>();
                    for (int i = 0; i < 9; i++) {
                        if (this.craftInv.getStackInSlot(i) != null && x != i) {
                            items.add(this.craftInv.getStackInSlot(i));
                        }
                    }

                    ItemStack result = TicUtil.canModifyItem(tool, items.toArray(new ItemStack[0]));
                    if (result == null) {
                        super.doClick(action, who);
                        return;
                    }
                    Ae2Reflect.makeItem(this, who, result);
                    who.openContainer.onCraftMatrixChanged(this.craftInv);
                    AdaptorPlayerHand ia = new AdaptorPlayerHand(who);
                    ia.addItems(result);
                    return;
                }
            }
        }
        super.doClick(action, who);
    }
}
