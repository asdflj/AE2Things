package com.asdflj.ae2thing.client.gui.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.nbt.NBTTagCompound;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.common.tile.TileWirelessDistributor;
import com.asdflj.ae2thing.inventory.item.INetworkTerminal;
import com.asdflj.ae2thing.network.SPacketNBTDataUpdate;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.util.DimensionalCoord;
import appeng.util.Platform;

public class ContainerWirelessDistributor extends BaseNetworkContainer implements INetworkTerminal {

    private final TileWirelessDistributor tile;

    public ContainerWirelessDistributor(InventoryPlayer ip, TileWirelessDistributor host) {
        super(ip, host);
        this.tile = (TileWirelessDistributor) this.getTileEntity();
    }

    public void updateData() {
        if (Platform.isServer()) {
            if (this.getGrid() == null) return;
            // send to client
            sendToPlayer();
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityplayer) {
        return true;
    }

    private void sendToPlayer() {
        NBTTagCompound data = new NBTTagCompound();
        this.tile.writeAllAvailableTileEntityToNBT(data);
        AE2Thing.proxy.netHandler.sendTo(new SPacketNBTDataUpdate(data), (EntityPlayerMP) player);
    }

    @Override
    public void addCraftingToCrafters(ICrafting crafting) {
        super.addCraftingToCrafters(crafting);
        updateData();
    }

    public void doLink(NBTTagCompound tag) {
        if (tag == null) return;
        DimensionalCoord target = DimensionalCoord.readFromNBT((NBTTagCompound) tag.getTag(Constants.TARGET));
        this.tile.doLink(target);
        sendToPlayer();
    }

    public void setName(NBTTagCompound tag) {
        if (tag == null) return;
        DimensionalCoord target = DimensionalCoord.readFromNBT(tag);
        String name = tag.getString(Constants.NAME);
        this.tile.setTargetName(target, name);
        sendToPlayer();
    }

    public void doUnLink(NBTTagCompound tag) {
        if (tag == null) return;
        DimensionalCoord d = DimensionalCoord.readFromNBT((NBTTagCompound) tag.getTag("#0"));
        this.tile.doUnLink(d);
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
