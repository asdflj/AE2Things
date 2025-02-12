package com.asdflj.ae2thing.client.event;

import com.asdflj.ae2thing.api.AE2ThingAPI;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import cpw.mods.fml.common.eventhandler.Event;

public class CraftTracking extends Event {

    public CraftTracking() {}

    public CraftTracking(IItemList<IAEItemStack> items) {
        AE2ThingAPI.instance()
            .clearTrakingMissingItems();
        for (IAEItemStack stack : items) {
            AE2ThingAPI.instance()
                .addTrackingMissingItem(stack);
        }
    }
}
