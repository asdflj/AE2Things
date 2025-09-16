package com.asdflj.ae2thing.client.event;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import com.asdflj.ae2thing.api.AE2ThingAPI;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.item.AEItemStack;
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

    public CraftTracking(IAEItemStack stack) {
        AE2ThingAPI.instance()
            .terminal()
            .clearTrackingMissingItems();
        AE2ThingAPI.instance()
            .terminal()
            .addTrackingMissingItem(stack);
    }

    public CraftTracking(ItemStack stack) {
        this(AEItemStack.create(stack));
    }

    public IItemList<IAEItemStack> getItems() {
        return AE2ThingAPI.instance()
            .terminal()
            .getTrackingMissingItems();
    }

    public static void postEvent() {
        MinecraftForge.EVENT_BUS.post(new CraftTracking());
    }
}
