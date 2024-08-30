package com.asdflj.ae2thing.common.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.WirelessObject;
import com.asdflj.ae2thing.common.tabs.AE2ThingTabs;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.asdflj.ae2thing.inventory.item.WirelessConnectorTerminalInventory;
import com.asdflj.ae2thing.loader.IRegister;
import com.asdflj.ae2thing.util.NameConst;

import appeng.api.AEApi;
import appeng.core.localization.PlayerMessages;
import cpw.mods.fml.common.registry.GameRegistry;

public class ItemWirelessConnectorTerminal extends ItemBaseWirelessTerminal
    implements IRegister<ItemWirelessConnectorTerminal> {

    public ItemWirelessConnectorTerminal() {
        AEApi.instance()
            .registries()
            .wireless()
            .registerWirelessHandler(this);

        setUnlocalizedName(NameConst.ITEM_WIRELESS_CONNECTOR_TERMINAL);
        setTextureName(
            AE2Thing.resource(NameConst.ITEM_WIRELESS_CONNECTOR_TERMINAL)
                .toString());
    }

    protected GuiType guiGuiType(ItemStack item) {
        return GuiType.WIRELESS_CONNECTOR_TERMINAL_ITEM;
    }

    @Override
    public Object getInventory(ItemStack stack, World world, int x, int y, int z, EntityPlayer player) {
        try {
            return new WirelessObject(stack, world, x, y, z, player)
                .getInventory(WirelessConnectorTerminalInventory.class);
        } catch (Exception e) {
            player.addChatMessage(PlayerMessages.OutOfRange.get());
            return null;
        }
    }

    @Override
    public ItemWirelessConnectorTerminal register() {
        GameRegistry.registerItem(this, NameConst.ITEM_WIRELESS_CONNECTOR_TERMINAL, AE2Thing.MODID);
        setCreativeTab(AE2ThingTabs.INSTANCE);
        return this;
    }
}
