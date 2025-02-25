package com.asdflj.ae2thing.client.event;

import com.asdflj.ae2thing.api.AE2ThingAPI;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import cpw.mods.fml.common.eventhandler.Event;

public class CraftTracking extends Event {

    public CraftTracking() {}

    public CraftTracking(IItemList<IAEItemStack> items) {
        AE2ThingAPI.instance()
            .terminal()
            .clearTrackingMissingItems();
        for (IAEItemStack stack : items) {
            AE2ThingAPI.instance()
                .terminal()
                .addTrackingMissingItem(stack);
        }
    }

    public IItemList<IAEItemStack> getItems() {
        return AE2ThingAPI.instance()
            .terminal()
            .getTrackingMissingItems();
    }
}
