package com.asdflj.ae2thing.inventory.gui;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;

import com.asdflj.ae2thing.client.gui.GuiCraftingTerminal;
import com.asdflj.ae2thing.client.gui.GuiDiskClone;
import com.asdflj.ae2thing.client.gui.container.ContainerCraftingTerminal;
import com.asdflj.ae2thing.client.gui.container.ContainerDiskClone;
import com.asdflj.ae2thing.inventory.ItemDiskCloneInventory;
import com.google.common.collect.ImmutableList;

import appeng.api.storage.ITerminalHost;

public enum GuiType {

    BACKPACK_TERMINAL(new ItemGuiFactory<>(ITerminalHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerCraftingTerminal(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiCraftingTerminal(player.inventory, inv);
        }
    }),
    DISK_CLONE(new ItemGuiFactory<>(ItemDiskCloneInventory.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, ItemDiskCloneInventory inv) {
            return new ContainerDiskClone(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ItemDiskCloneInventory inv) {
            return new GuiDiskClone(player.inventory, inv);
        }
    });

    public static final List<GuiType> VALUES = ImmutableList.copyOf(values());

    @Nullable
    public static GuiType getByOrdinal(int ordinal) {
        return ordinal < 0 || ordinal >= VALUES.size() ? null : VALUES.get(ordinal);
    }

    public final IGuiFactory guiFactory;

    GuiType(IGuiFactory guiFactory) {
        this.guiFactory = guiFactory;
    }
}
