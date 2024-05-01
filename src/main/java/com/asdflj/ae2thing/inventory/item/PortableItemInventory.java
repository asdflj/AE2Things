package com.asdflj.ae2thing.inventory.item;

import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.asdflj.ae2thing.common.storage.CellInventory;
import com.asdflj.ae2thing.inventory.ItemBiggerAppEngInventory;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.MEMonitorHandler;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.IConfigManager;
import appeng.container.interfaces.IInventorySlotAware;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.ConfigManager;
import appeng.util.Platform;

public class PortableItemInventory extends MEMonitorHandler<IAEItemStack>
    implements ITerminalHost, IInventorySlotAware, IGuiItemObject, IEnergySource {

    private final ItemStack target;
    private final int inventorySlot;
    protected AppEngInternalInventory crafting;
    protected EntityPlayer player;

    public PortableItemInventory(ItemStack is, int slot, EntityPlayer player) {
        super(Objects.requireNonNull(CellInventory.getCell(is, null, player)));
        this.target = is;
        this.inventorySlot = slot;
        this.player = player;
        this.crafting = new ItemBiggerAppEngInventory(is, "crafting", 9, player, slot);
    }

    @Override
    public int getInventorySlot() {
        return this.inventorySlot;
    }

    @Override
    public ItemStack getItemStack() {
        return this.target;
    }

    @Override
    public IMEMonitor<IAEItemStack> getItemInventory() {
        return this;
    }

    @Override
    public IMEMonitor<IAEFluidStack> getFluidInventory() {
        return null;
    }

    private void saveSettings() {
        this.player.inventory.setInventorySlotContents(this.inventorySlot, this.target);
    }

    @Override
    public IConfigManager getConfigManager() {
        final ConfigManager out = new ConfigManager((manager, settingName, newValue) -> {
            final NBTTagCompound data = Platform.openNbtData(this.target);
            manager.writeToNBT(data);
            saveSettings();
        });
        out.registerSetting(Settings.SORT_BY, SortOrder.NAME);
        out.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
        out.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);
        out.readFromNBT(
            (NBTTagCompound) Platform.openNbtData(this.target)
                .copy());
        return out;
    }

    public IInventory getInventoryByName(String crafting) {
        if (crafting.equals("crafting")) {
            return this.crafting;
        }
        return null;
    }

    @Override
    public double extractAEPower(double amt, Actionable mode, PowerMultiplier usePowerMultiplier) {
        amt = usePowerMultiplier.multiply(amt);
        return usePowerMultiplier.divide(amt);
    }
}
