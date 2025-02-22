package com.asdflj.ae2thing.client.gui.container;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;

import appeng.api.config.SecurityPermissions;
import appeng.api.storage.ITerminalHost;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.SlotInaccessible;
import appeng.tile.inventory.AppEngInternalInventory;

public class ContainerPatternValueName extends AEBaseContainer {

    private final Slot patternValue;

    @GuiSync(11)
    public int valueIndex;

    public ContainerPatternValueName(final InventoryPlayer ip, final ITerminalHost te) {
        super(ip, te);
        this.patternValue = new SlotInaccessible(new AppEngInternalInventory(null, 1), 0, 13, 29);
        this.addSlotToContainer(patternValue);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        this.verifyPermissions(SecurityPermissions.CRAFT, false);
    }

    public Slot getPatternValue() {
        return patternValue;
    }

    public int getValueIndex() {
        return valueIndex;
    }

    public void setValueIndex(int valueIndex) {
        this.valueIndex = valueIndex;
    }
}
