package com.asdflj.ae2thing.client.gui.container.BaseMonitor;

import net.minecraft.inventory.ICrafting;

import appeng.api.storage.IMEMonitor;

public interface IProcessItemList {

    void processItemList();

    void queueInventory(final ICrafting c);

    void removeCraftingFromCrafters(ICrafting c);

    void addListener();

    void removeListener();

    IMEMonitor getMonitor();

}
