package com.asdflj.ae2thing.api.adapter.terminal.item;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.api.features.ILocatable;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.features.IWirelessTermRegistry;
import appeng.core.localization.PlayerMessages;

public interface ITerminalHandler {

    default boolean canConnect(ItemStack item, ITerminalHandler terminal, TerminalItems items, EntityPlayerMP player) {
        IWirelessTermRegistry term = AEApi.instance()
            .registries()
            .wireless();
        if (!term.isWirelessTerminal(item)) {
            player.addChatMessage(PlayerMessages.DeviceNotWirelessTerminal.get());
            return false;
        }
        final IWirelessTermHandler handler = term.getWirelessTerminalHandler(item);
        final String unparsedKey = handler.getEncryptionKey(item);
        if (unparsedKey.isEmpty()) {
            player.addChatMessage(PlayerMessages.DeviceNotLinked.get());
            return false;
        }
        final long parsedKey = Long.parseLong(unparsedKey);
        final ILocatable securityStation = AEApi.instance()
            .registries()
            .locatable()
            .getLocatableBy(parsedKey);
        if (securityStation == null) {
            player.addChatMessage(PlayerMessages.StationCanNotBeLocated.get());
            return false;
        }
        if (handler.hasPower(player, 0.5, item)) {
            return true;
        } else {
            player.addChatMessage(PlayerMessages.DeviceNotPowered.get());
        }
        return false;
    }

    void openGui(ItemStack item, ITerminalHandler terminal, TerminalItems items, EntityPlayerMP player);
}
