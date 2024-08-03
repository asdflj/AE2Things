package com.asdflj.ae2thing.client.gui.container;

import java.util.ArrayList;
import java.util.List;

import net.bdew.ae2stuff.grid.Security;
import net.bdew.ae2stuff.machines.wireless.TileWireless;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.inventory.WirelessConnectorTerminal;
import com.asdflj.ae2thing.network.SPacketWirelessConnectorUpdate;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;
import appeng.api.storage.ITerminalHost;
import appeng.container.AEBaseContainer;
import appeng.hooks.TickHandler;
import appeng.me.Grid;
import appeng.util.Platform;

public class ContainerWirelessConnectorTerminal extends AEBaseContainer {

    private final EntityPlayer player;
    private final WirelessConnectorTerminal terminal;

    public ContainerWirelessConnectorTerminal(InventoryPlayer ip, ITerminalHost host) {
        super(ip, host);
        this.player = ip.player;
        terminal = (WirelessConnectorTerminal) host;
    }

    public void updateData() {
        if (Platform.isServer()) {
            int playerID = Security.getPlayerId(player.getGameProfile());
            List<TileWireless> wirelessTiles = new ArrayList<>();
            for (Grid grid : TickHandler.INSTANCE.getGridList()) {
                IMachineSet set = grid.getMachines(TileWireless.class);
                if (set.isEmpty()) continue;
                for (IGridNode node : set) {
                    TileWireless tile = (TileWireless) node.getGridBlock();
                    if (terminal.getGrid() != null && terminal.getGrid()
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
            AE2Thing.proxy.netHandler
                .sendTo(new SPacketWirelessConnectorUpdate(wirelessTiles), (EntityPlayerMP) player);
        }
    }

    @Override
    public void addCraftingToCrafters(ICrafting crafting) {
        updateData();
        super.addCraftingToCrafters(crafting);
    }

    public boolean doLink(TileWireless left, TileWireless right) {
        if (left == null || right == null || left == right) return false;
        if (left.isLinked()) left.doUnlink();
        if (right.isLinked()) right.doUnlink();
        left.doLink(right);
        return true;
    }

    public void doUnlink(TileWireless tile) {
        if (tile == null) return;
        if (tile.isLinked()) tile.doUnlink();
    }
}
