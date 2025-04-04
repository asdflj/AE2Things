package com.asdflj.ae2thing.client.event;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.util.NameConst;

import appeng.api.storage.data.IAEItemStack;
import appeng.util.Platform;
import cpw.mods.fml.common.eventhandler.Event;

public class NotificationEvent extends Event {

    private final String tile;
    private final String content;
    private final ItemStack item;

    public NotificationEvent(String tile, String Content, ItemStack item) {
        this.tile = tile;
        this.content = Content;
        this.item = item;
    }

    public NotificationEvent(IAEItemStack item) {
        this(
            I18n.format(NameConst.MESSAGE_CRAFTING_COMPLETE),
            String.format(
                "%s %s",
                AE2ThingAPI.readableNumber.toWideReadableForm(item.getStackSize()),
                Platform.getItemDisplayName(item)),
            item.getItemStack());
    }

    public String getTile() {
        return tile;
    }

    public String getContent() {
        return content;
    }

    public ItemStack getItem() {
        return item;
    }

}
