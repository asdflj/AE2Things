package com.asdflj.ae2thing.inventory.gui;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;

import com.asdflj.ae2thing.client.gui.GuiCellLink;
import com.asdflj.ae2thing.client.gui.GuiCraftAmount;
import com.asdflj.ae2thing.client.gui.GuiCraftConfirm;
import com.asdflj.ae2thing.client.gui.GuiCraftingStatus;
import com.asdflj.ae2thing.client.gui.GuiCraftingTerminal;
import com.asdflj.ae2thing.client.gui.GuiFluidPacketEncoder;
import com.asdflj.ae2thing.client.gui.GuiInfusionPatternTerminal;
import com.asdflj.ae2thing.client.gui.GuiManaIO;
import com.asdflj.ae2thing.client.gui.GuiPatternValueAmount;
import com.asdflj.ae2thing.client.gui.GuiPatternValueName;
import com.asdflj.ae2thing.client.gui.GuiRenamer;
import com.asdflj.ae2thing.client.gui.GuiWirelessConnectorTerminal;
import com.asdflj.ae2thing.client.gui.GuiWirelessDualInterfaceTerminal;
import com.asdflj.ae2thing.client.gui.container.ContainerCellLink;
import com.asdflj.ae2thing.client.gui.container.ContainerCraftConfirm;
import com.asdflj.ae2thing.client.gui.container.ContainerCraftingTerminal;
import com.asdflj.ae2thing.client.gui.container.ContainerFluidPacketEncoder;
import com.asdflj.ae2thing.client.gui.container.ContainerInfusionPatternTerminal;
import com.asdflj.ae2thing.client.gui.container.ContainerManaIO;
import com.asdflj.ae2thing.client.gui.container.ContainerPatternValueAmount;
import com.asdflj.ae2thing.client.gui.container.ContainerPatternValueName;
import com.asdflj.ae2thing.client.gui.container.ContainerRenamer;
import com.asdflj.ae2thing.client.gui.container.ContainerWirelessConnectorTerminal;
import com.asdflj.ae2thing.client.gui.container.ContainerWirelessDualInterfaceTerminal;
import com.asdflj.ae2thing.common.parts.SharedManaBus;
import com.asdflj.ae2thing.common.parts.THPart;
import com.asdflj.ae2thing.common.tile.TileFluidPacketEncoder;
import com.asdflj.ae2thing.inventory.ItemCellLinkInventory;
import com.google.common.collect.ImmutableList;

import appeng.api.storage.ITerminalHost;
import appeng.container.implementations.ContainerCraftAmount;
import appeng.container.implementations.ContainerCraftingStatus;

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
    WIRELESS_CONNECTOR_TERMINAL(new PartGuiFactory<>(ITerminalHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerWirelessConnectorTerminal(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiWirelessConnectorTerminal(player.inventory, inv);
        }
    }),
    WIRELESS_CONNECTOR_TERMINAL_ITEM(new ItemGuiFactory<>(ITerminalHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerWirelessConnectorTerminal(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiWirelessConnectorTerminal(player.inventory, inv);
        }
    }),
    WIRELESS_DUAL_INTERFACE_TERMINAL(new ItemGuiFactory<>(ITerminalHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerWirelessDualInterfaceTerminal(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiWirelessDualInterfaceTerminal(player.inventory, inv);
        }
    }),
    INFUSION_PATTERN_TERMINAL(new PartGuiFactory<>(ITerminalHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerInfusionPatternTerminal(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiInfusionPatternTerminal(player.inventory, inv);
        }
    }),
    CRAFTING_CONFIRM(new PartGuiFactory<>(THPart.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, THPart inv) {
            return new ContainerCraftConfirm(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, THPart inv) {
            return new GuiCraftConfirm(player.inventory, inv);
        }
    }),
    CRAFTING_CONFIRM_ITEM(new ItemGuiFactory<>(ITerminalHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerCraftConfirm(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiCraftConfirm(player.inventory, inv);
        }
    }),
    RENAMER(new ItemGuiFactory<>(ITerminalHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerRenamer(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiRenamer(player.inventory, inv);
        }
    }),
    CRAFTING_STATUS(new PartGuiFactory<>(ITerminalHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerCraftingStatus(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiCraftingStatus(player.inventory, inv);
        }
    }),
    CRAFTING_STATUS_ITEM(new ItemGuiFactory<>(ITerminalHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerCraftingStatus(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiCraftingStatus(player.inventory, inv);
        }
    }),
    PATTERN_VALUE_SET(new PartGuiFactory<>(ITerminalHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerPatternValueAmount(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiPatternValueAmount(player.inventory, inv);
        }
    }),
    PATTERN_VALUE_SET_ITEM(new ItemGuiFactory<>(ITerminalHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerPatternValueAmount(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiPatternValueAmount(player.inventory, inv);
        }
    }),
    PATTERN_NAME_SET(new PartGuiFactory<>(ITerminalHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerPatternValueName(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiPatternValueName(player.inventory, inv);
        }
    }),
    PATTERN_NAME_SET_ITEM(new ItemGuiFactory<>(ITerminalHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerPatternValueName(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiPatternValueName(player.inventory, inv);
        }
    }),

    CRAFTING_AMOUNT(new PartGuiFactory<>(ITerminalHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerCraftAmount(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiCraftAmount(player.inventory, inv);
        }
    }),
    CRAFTING_AMOUNT_ITEM(new ItemGuiFactory<>(ITerminalHost.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerCraftAmount(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiCraftAmount(player.inventory, inv);
        }
    }),
    MANA_BUS_IO(new PartGuiFactory<>(SharedManaBus.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, SharedManaBus inv) {
            return new ContainerManaIO(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, SharedManaBus inv) {
            return new GuiManaIO(player.inventory, inv);
        }
    }),
    CELL_LINK(new ItemGuiFactory<>(ItemCellLinkInventory.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, ItemCellLinkInventory inv) {
            return new ContainerCellLink(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ItemCellLinkInventory inv) {
            return new GuiCellLink(player.inventory, inv);
        }
    }),
    FLUID_PACKET_ENCODER(new TileGuiFactory<>(TileFluidPacketEncoder.class) {

        @Override
        protected Object createServerGui(EntityPlayer player, TileFluidPacketEncoder inv) {
            return new ContainerFluidPacketEncoder(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, TileFluidPacketEncoder inv) {
            return new GuiFluidPacketEncoder(player.inventory, inv);
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
