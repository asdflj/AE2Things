package com.asdflj.ae2thing.common.item;

import java.util.EnumSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.WirelessObject;
import com.asdflj.ae2thing.common.tabs.AE2ThingTabs;
import com.asdflj.ae2thing.inventory.InventoryHandler;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.asdflj.ae2thing.inventory.item.IItemInventory;
import com.asdflj.ae2thing.loader.IRegister;
import com.asdflj.ae2thing.util.BlockPos;
import com.asdflj.ae2thing.util.NameConst;

import appeng.api.AEApi;
import appeng.api.features.ILocatable;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.features.IWirelessTermRegistry;
import appeng.core.features.AEFeature;
import appeng.core.localization.PlayerMessages;
import appeng.items.tools.powered.ToolWirelessTerminal;
import appeng.util.Platform;
import cpw.mods.fml.common.registry.GameRegistry;

public class ItemWirelessConnectorTerminal extends ToolWirelessTerminal
    implements IItemInventory, IRegister<ItemWirelessConnectorTerminal> {

    public ItemWirelessConnectorTerminal() {
        AEApi.instance()
            .registries()
            .wireless()
            .registerWirelessHandler(this);
        this.setFeature(EnumSet.of(AEFeature.WirelessAccessTerminal, AEFeature.PoweredTools));
        setUnlocalizedName(NameConst.ITEM_WIRELESS_CONNECTOR_TERMINAL);
        setTextureName(
            AE2Thing.resource(NameConst.ITEM_WIRELESS_CONNECTOR_TERMINAL)
                .toString());
    }

    public ItemStack stack() {
        return new ItemStack(this, 1);
    }

    @Override
    public ItemStack onItemRightClick(final ItemStack item, final World w, final EntityPlayer player) {
        if (ForgeEventFactory.onItemUseStart(player, item, 1) > 0) {
            if (Platform.isClient()) return item;
            IWirelessTermRegistry term = AEApi.instance()
                .registries()
                .wireless();
            if (!term.isWirelessTerminal(item)) {
                player.addChatMessage(PlayerMessages.DeviceNotWirelessTerminal.get());
                return item;
            }
            final IWirelessTermHandler handler = term.getWirelessTerminalHandler(item);
            final String unparsedKey = handler.getEncryptionKey(item);
            if (unparsedKey.isEmpty()) {
                player.addChatMessage(PlayerMessages.DeviceNotLinked.get());
                return item;
            }
            final long parsedKey = Long.parseLong(unparsedKey);
            final ILocatable securityStation = AEApi.instance()
                .registries()
                .locatable()
                .getLocatableBy(parsedKey);
            if (securityStation == null) {
                player.addChatMessage(PlayerMessages.StationCanNotBeLocated.get());
                return item;
            }
            if (handler.hasPower(player, 0.5, item)) {
                InventoryHandler.openGui(
                    player,
                    w,
                    new BlockPos(player.inventory.currentItem, 0, 0),
                    ForgeDirection.UNKNOWN,
                    this.guiGuiType(item));
            } else {
                player.addChatMessage(PlayerMessages.DeviceNotPowered.get());
            }
        }

        return item;
    }

    private GuiType guiGuiType(ItemStack item) {
        return GuiType.WIRELESS_CONNECTOR_TERMINAL_ITEM;
    }

    @Override
    public boolean canHandle(final ItemStack is) {
        return is.getItem() instanceof ItemWirelessConnectorTerminal;
    }

    @Override
    public Object getInventory(ItemStack stack, World world, int x, int y, int z, EntityPlayer player) {
        try {
            return new WirelessObject(stack, world, x, y, z, player).getInventory();
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
