package com.asdflj.ae2thing.client.gui.container;

import java.util.ArrayList;
import java.util.List;

import net.bdew.ae2stuff.grid.Security;
import net.bdew.ae2stuff.machines.wireless.TileWireless;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.inventory.item.INetworkTerminal;
import com.asdflj.ae2thing.network.SPacketWirelessConnectorUpdate;
import com.asdflj.ae2thing.util.Util;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;
import appeng.api.storage.ITerminalHost;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import appeng.hooks.TickHandler;
import appeng.me.Grid;
import appeng.util.Platform;

public class ContainerWirelessConnectorTerminal extends BaseNetworkContainer implements INetworkTerminal {

    private final List<TileWireless> wirelessTiles = new ArrayList<>();

    public ContainerWirelessConnectorTerminal(InventoryPlayer ip, ITerminalHost host) {
        super(ip, host);
    }

    public void updateData() {
        if (Platform.isServer()) {
            wirelessTiles.clear();
            if (!hasPower) return;
            if (this.getGrid() == null) return;
            int playerID = Security.getPlayerId(player.getGameProfile());
            for (Grid grid : TickHandler.INSTANCE.getGridList()) {
                IMachineSet set = grid.getMachines(TileWireless.class);
                if (set.isEmpty()) continue;
                for (IGridNode node : set) {
                    TileWireless tile = (TileWireless) node.getGridBlock();
                    if (this.getGrid() != null && this.getGrid()
                        .equals(grid)) {
                        wirelessTiles.add(tile);
                    } else {
                        int id = node.getPlayerID();
                        if (id == -1 || id != playerID) continue; // is not your block
                        wirelessTiles.add(tile);
                    }
                }
            }
            // send to client
            sendToPlayer();
        }
    }

    private void sendToPlayer() {
        AE2Thing.proxy.netHandler.sendTo(new SPacketWirelessConnectorUpdate(wirelessTiles), (EntityPlayerMP) player);
    }

    @Override
    public void addCraftingToCrafters(ICrafting crafting) {
        super.addCraftingToCrafters(crafting);
        updateData();
    }

    public void doLink(TileWireless left, TileWireless right) {
        if (left == null || right == null || left == right) return;
        if (left.isLinked()) left.doUnlink();
        if (right.isLinked()) right.doUnlink();
        try {
            left.doLink(right);
        } catch (Exception e) {
            left.doUnlink();
            right.doUnlink();
            ChatComponentText s = new ChatComponentText(
                I18n.format("ae2stuff.wireless.tool.failed") + ": " + e.getMessage());
            s.getChatStyle()
                .setColor(EnumChatFormatting.RED);
            this.player.addChatComponentMessage(s);
        }
    }

    public void doUnlink(TileWireless tile) {
        if (tile == null) return;
        if (tile.isLinked()) tile.doUnlink();
    }

    public void setName(String name, NBTTagCompound tag) {
        if (tag == null) return;
        DimensionalCoord d = DimensionalCoord.readFromNBT(tag);
        for (TileWireless tile : wirelessTiles) {
            if (Util.isSameDimensionalCoord(tile.getLocation(), d)) {
                tile.setCustomName(name);
                sendToPlayer();
            }
        }
    }

    public void bind(NBTTagCompound tag) {
        if (tag == null) return;
        DimensionalCoord a = DimensionalCoord.readFromNBT((NBTTagCompound) tag.getTag("#0"));
        DimensionalCoord b = DimensionalCoord.readFromNBT((NBTTagCompound) tag.getTag("#1"));
        TileWireless ta = null;
        TileWireless tb = null;
        for (TileWireless tile : wirelessTiles) {
            if (Util.isSameDimensionalCoord(tile.getLocation(), a)) {
                ta = tile;
            } else if (Util.isSameDimensionalCoord(tile.getLocation(), b)) {
                tb = tile;
            }
            if (ta != null && tb != null) {
                doLink(ta, tb);
                sendToPlayer();
            }
        }
    }

    public void unBind(NBTTagCompound tag) {
        if (tag == null) return;
        DimensionalCoord d = DimensionalCoord.readFromNBT((NBTTagCompound) tag.getTag("#0"));
        for (TileWireless tile : wirelessTiles) {
            if (Util.isSameDimensionalCoord(tile.getLocation(), d)) {
                doUnlink(tile);
                sendToPlayer();
            }
        }
    }

    public void setColor(NBTTagCompound tag) {
        if (tag == null) return;
        NBTTagCompound data = (NBTTagCompound) tag.getTag("#0");
        DimensionalCoord a = DimensionalCoord.readFromNBT(data);
        AEColor color = AEColor.values()[data.getShort(Constants.COLOR)];
        for (TileWireless tile : wirelessTiles) {
            if (Util.isSameDimensionalCoord(tile.getLocation(), a)) {
                tile.color_$eq(color);
                tile.getWorldObject()
                    .markBlockForUpdate(a.x, a.y, a.z);
                sendToPlayer();
            }
        }
    }

    @Override
    public IGrid getGrid() {
        return getGridNode().getGrid();
    }

    @Override
    public IGridNode getGridNode() {
        return this.getActionHost()
            .getActionableNode();
    }
}
