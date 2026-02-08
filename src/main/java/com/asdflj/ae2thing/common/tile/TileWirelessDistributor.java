package com.asdflj.ae2thing.common.tile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import com.asdflj.ae2thing.util.BlockPos;

import appeng.api.AEApi;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridNode;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkTile;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class TileWirelessDistributor extends AENetworkTile implements IGridTickable {

    public final static class GridConnectionWrapper {

        public BlockPos pos;
        public IGridConnection connection;
        public TileEntity tile;

        public GridConnectionWrapper(TileEntity tile, IGridConnection connection) {
            this.pos = new BlockPos(tile);
            this.tile = tile;
            this.connection = connection;
        }
    }

    private final List<GridConnectionWrapper> grids = new ArrayList<>();

    private final List<DimensionalCoord> dimensionalCoords = new ArrayList<>();
    private static int tick = 0;
    private int usedChannels = 0;

    public TileWirelessDistributor() {
        this.getProxy()
            .setFlags(GridFlags.DENSE_CAPACITY);
    }

    public int getUsedChannels() {
        return usedChannels;
    }

    @TileEvent(TileEventType.NETWORK_READ)
    public boolean readFromNBTEvent(final ByteBuf buf) {
        dimensionalCoords.clear();
        try {
            ByteArrayInputStream bytes = new ByteArrayInputStream(
                buf.readBytes(buf.readableBytes())
                    .array());
            NBTTagCompound data = CompressedStreamTools.readCompressed(bytes);
            NBTTagList list = data.getTagList("grids", 10);
            for (int x = 0; x < list.tagCount(); x++) {
                final NBTTagCompound tag = list.getCompoundTagAt(x);
                DimensionalCoord dimensionalCoord = DimensionalCoord.readFromNBT(tag);
                dimensionalCoords.add(dimensionalCoord);
            }
        } catch (IOException ignored) {}
        return false;
    }

    public List<DimensionalCoord> getDimensionalCoords() {
        return dimensionalCoords;
    }

    @TileEvent(TileEventType.NETWORK_WRITE)
    public void writeToNBTEvent(final ByteBuf buf) {
        NBTTagCompound data = new NBTTagCompound();
        NBTTagList list = new NBTTagList();
        for (GridConnectionWrapper gc : grids) {
            NBTTagCompound tag = new NBTTagCompound();
            gc.connection.b()
                .getGridBlock()
                .getLocation()
                .writeToNBT(tag);
            list.appendTag(tag);
        }
        data.setTag("grids", list);
        compressedData(buf, data);
    }

    private void compressedData(final ByteBuf buf, NBTTagCompound tag) {
        try {
            final ByteBuf data = Unpooled.buffer();
            final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            final DataOutputStream outputStream = new DataOutputStream(bytes);

            CompressedStreamTools.writeCompressed(tag, outputStream);
            data.writeBytes(bytes.toByteArray());
            data.capacity(data.readableBytes());
            buf.writeBytes(data);

        } catch (IOException ignored) {

        }
    }

    @MENetworkEventSubscribe
    public void onChannelUpdate(MENetworkChannelsChanged event) {
        if (MinecraftServer.getServer()
            .getTickCounter() == tick) {
            return;
        }
        tick = MinecraftServer.getServer()
            .getTickCounter();
        List<GridConnectionWrapper> list = new ArrayList<>();
        for (GridConnectionWrapper wrapper : grids) {
            if (wrapper.pos.getTileEntity() == null || wrapper.tile == null
                || wrapper.pos.getTileEntity() != wrapper.tile) {
                wrapper.connection.destroy();
            } else {
                list.add(wrapper);
            }
        }
        grids.clear();
        grids.addAll(list);

        this.usedChannels = 0;
        for (GridConnectionWrapper wrapper : grids) {
            this.usedChannels += wrapper.connection.getUsedChannels();
        }
        this.markForUpdate();
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(60, 60, false, true);
    }

    public List<GridConnectionWrapper> getGrids() {
        return grids;
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall) {
        if (this.getProxy()
            .isActive()) {
            World world = this.getTile()
                .getWorldObj();
            Chunk chunk = world.getChunkFromBlockCoords(this.xCoord, this.zCoord);
            for (Object obj : chunk.chunkTileEntityMap.values()) {
                if (obj instanceof BaseMetaTileEntity tile && tile.getProxy() != null
                    && tile.getProxy()
                        .isReady()
                    && !tile.getProxy()
                        .isActive()) {
                    try {
                        grids.add(
                            new GridConnectionWrapper(
                                tile,
                                AEApi.instance()
                                    .createGridConnection(
                                        this.getActionableNode(),
                                        tile.getProxy()
                                            .getNode())));
                    } catch (Exception ignored) {}
                } else if (obj instanceof TileWirelessDistributor && obj != this) {
                    world.func_147480_a(this.xCoord, this.yCoord, this.zCoord, true);
                    break;
                }
            }
        }

        return TickRateModulation.SAME;
    }

    public int getMaxChannels() {
        return ((GridNode) this.getProxy()
            .getNode()).getMaxChannels();
    }
}
