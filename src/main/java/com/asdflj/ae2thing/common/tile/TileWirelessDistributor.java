package com.asdflj.ae2thing.common.tile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraftforge.common.util.ForgeDirection;

import com.asdflj.ae2thing.api.Constants;
import com.asdflj.ae2thing.common.block.SubBlocks;
import com.asdflj.ae2thing.util.BlockPos;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.exceptions.FailedConnection;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.implementations.tiles.IColorableTile;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkChannelChanged;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.me.GridNode;
import appeng.me.helpers.IGridProxyable;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkTile;
import appeng.util.Platform;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class TileWirelessDistributor extends AENetworkTile
    implements IGridTickable, IColorableTile, SubBlocks, IPowerChannelState {

    private int damage = 0;
    private final List<GridConnectionWrapper> connectionWrappers = new ArrayList<>();
    private final List<DimensionalCoord> dimensionalCoords = new ArrayList<>();
    private int usedChannels = 0;
    private boolean loaded = false;
    private boolean scanning = false;
    private Constants.Radius radius = Constants.Radius.Tier2;
    private boolean isPowered = false;
    private static final int MAX_SIZE = 40;

    @Override
    public AEColor getColor() {
        return AEColor.values()[this.getDamage()];
    }

    public void alert() {
        try {
            this.scanning = true;
            this.getProxy()
                .getTick()
                .alertDevice(this.getActionableNode());
            this.markForUpdate();
        } catch (Exception ignored) {}

    }

    @Override
    public boolean recolourBlock(ForgeDirection side, AEColor color, EntityPlayer who) {
        if (this.getColor()
            .equals(color)) {
            return false;
        } else {
            this.setDamage(color.ordinal());
            if (Platform.isServer()) {
                this.getProxy()
                    .getNode()
                    .updateState();
            }
            return true;
        }
    }

    public void setTargetName(DimensionalCoord target, String name) {
        TileEntity tile = this.getWorldObj()
            .getTileEntity(target.x, target.y, target.z);
        if (!(tile instanceof AENetworkTile)) {
            return;
        }
        for (GridConnectionWrapper c : this.connectionWrappers) {
            if (c.tile.equals(tile)) {
                ((AENetworkTile) tile).setCustomName(name);
                return;
            }
        }
    }

    @Override
    public boolean isPowered() {
        return this.isPowered;
    }

    @Override
    public boolean isActive() {
        return this.isPowered;
    }

    public final static class GridConnectionWrapper {

        public BlockPos pos;
        public IGridConnection connection;
        public TileEntity tile;

        public GridConnectionWrapper(TileEntity tile, IGridConnection connection) {
            this.pos = new BlockPos(tile);
            this.tile = tile;
            this.connection = connection;
        }

        public void writeToNBT(NBTTagCompound tag) {
            pos.getDimensionalCoord()
                .writeToNBT(tag);
        }

        public static GridConnectionWrapper readFromNBT(IGridProxyable self, World w, NBTTagCompound tag) {
            return readFromNBT(self, w, DimensionalCoord.readFromNBT(tag));
        }

        public static GridConnectionWrapper readFromNBT(IGridProxyable self, World w, DimensionalCoord coord) {
            try {
                TileEntity entity = w.getTileEntity(coord.x, coord.y, coord.z);
                if (entity instanceof IGridProxyable target) {
                    return doLink(self, entity, target);
                }
            } catch (Exception ignored) {}

            return null;
        }
    }

    public int getUsedChannels() {
        return usedChannels;
    }

    public TileWirelessDistributor() {
        super();
        this.getProxy()
            .setFlags(GridFlags.DENSE_CAPACITY);
    }

    @Override
    public int getDamage() {
        return damage;
    }

    @Override
    public void setDamage(int damage) {
        this.damage = damage;
        this.getProxy()
            .setColor(AEColor.values()[damage]);
        if (Platform.isClient()) {
            this.worldObj.markBlockRangeForRenderUpdate(
                this.xCoord,
                this.yCoord,
                this.zCoord,
                this.xCoord,
                this.yCoord,
                this.zCoord);
        }
    }

    @TileEvent(TileEventType.NETWORK_READ)
    public boolean readFromNBTEvent(final ByteBuf buf) {
        dimensionalCoords.clear();
        try {
            ByteArrayInputStream bytes = new ByteArrayInputStream(
                buf.readBytes(buf.readableBytes())
                    .array());
            NBTTagCompound data = CompressedStreamTools.readCompressed(bytes);
            this.setDamage(data.getInteger(Constants.COLOR));
            NBTTagList list = data.getTagList(Constants.CONNECTIONS, 10);
            for (int x = 0; x < list.tagCount(); x++) {
                final NBTTagCompound tag = list.getCompoundTagAt(x);
                DimensionalCoord dimensionalCoord = DimensionalCoord.readFromNBT(tag);
                dimensionalCoords.add(dimensionalCoord);
            }
            this.isPowered = data.getBoolean(Constants.POWERED);
        } catch (IOException ignored) {}
        return false;
    }

    public List<DimensionalCoord> getDimensionalCoords() {
        return dimensionalCoords;
    }

    @TileEvent(TileEventType.WORLD_NBT_READ)
    public void readFromNBTEvent(NBTTagCompound data) {
        this.setDamage(data.getInteger(Constants.COLOR));
        this.radius = Constants.Radius.values()[data.getInteger(Constants.TIER)];
        NBTTagList list = data.getTagList(Constants.CONNECTIONS, 10);
        if (Platform.isServer() && !this.loaded) {
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound tag = list.getCompoundTagAt(i);
                this.dimensionalCoords.add(DimensionalCoord.readFromNBT(tag));
            }
            this.loaded = true;
        }
    }

    @Override
    public void onReady() {
        super.onReady();
        if (Platform.isServer()) {
            for (DimensionalCoord coord : dimensionalCoords) {
                GridConnectionWrapper wrapper = GridConnectionWrapper.readFromNBT(this, this.getWorldObj(), coord);
                if (wrapper != null) {
                    this.connectionWrappers.add(wrapper);
                }
            }
            this.dimensionalCoords.clear();
        }
    }

    @TileEvent(TileEventType.WORLD_NBT_WRITE)
    public NBTTagCompound writeToNBTEvent(NBTTagCompound data) {
        data.setInteger(Constants.COLOR, this.getDamage());
        data.setInteger(Constants.TIER, this.radius.ordinal());
        NBTTagList list = new NBTTagList();
        data.setTag(Constants.CONNECTIONS, list);
        for (var con : this.connectionWrappers) {
            NBTTagCompound tag = new NBTTagCompound();
            con.writeToNBT(tag);
            list.appendTag(tag);
        }
        return data;
    }

    @MENetworkEventSubscribe
    public void stateChange(final MENetworkPowerStatusChange p) {
        updatePowerState();
    }

    private void updatePowerState() {
        boolean newState = false;

        try {
            newState = getProxy().isActive() && getProxy().getEnergy()
                .extractAEPower(1, Actionable.SIMULATE, PowerMultiplier.CONFIG) > 0.0001;
        } catch (final GridAccessException ignored) {

        }
        if (newState != isPowered) {
            isPowered = newState;
            markForUpdate();
        }
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
        super.onDataPacket(net, packet);
        this.worldObj.markBlockRangeForRenderUpdate(
            this.xCoord,
            this.yCoord,
            this.zCoord,
            this.xCoord,
            this.yCoord,
            this.zCoord);

    }

    @TileEvent(TileEventType.NETWORK_WRITE)
    public void writeToNBTEvent(final ByteBuf buf) {
        this.getProxy()
            .setColor(AEColor.values()[this.getDamage()]);
        NBTTagCompound data = new NBTTagCompound();
        NBTTagList list = new NBTTagList();
        for (GridConnectionWrapper gc : connectionWrappers) {
            NBTTagCompound tag = new NBTTagCompound();
            gc.connection.b()
                .getGridBlock()
                .getLocation()
                .writeToNBT(tag);
            list.appendTag(tag);
        }
        data.setBoolean(Constants.POWERED, this.isPowered);
        data.setTag(Constants.CONNECTIONS, list);
        data.setInteger(Constants.COLOR, this.getDamage());
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

    private static boolean isSameColor(AEColor c1, AEColor c2) {
        return c1.equals(AEColor.Transparent) || c2.equals(AEColor.Transparent) || c1.equals(c2);
    }

    @MENetworkEventSubscribe
    public void onChannelUpdate(MENetworkChannelsChanged event) {
        List<GridConnectionWrapper> list = new ArrayList<>();
        for (GridConnectionWrapper wrapper : connectionWrappers) {
            if (wrapper.pos.getTileEntity() == null || wrapper.tile == null
                || wrapper.pos.getTileEntity() != wrapper.tile
                || !(wrapper.tile instanceof IGridProxyable proxy && isSameColor(
                    proxy.getProxy()
                        .getColor(),
                    this.getColor()))) {
                wrapper.connection.destroy();
            } else {
                list.add(wrapper);
            }
        }
        connectionWrappers.clear();
        connectionWrappers.addAll(list);
        updatePowerState();
        this.markForUpdate();
    }

    public void updateUsedChannels() {
        this.usedChannels = 0;
        if (this.getProxy()
            .isActive()) {
            for (GridConnectionWrapper wrapper : connectionWrappers) {
                this.usedChannels += wrapper.connection.getUsedChannels();
            }
        }
    }

    @MENetworkEventSubscribe
    public void onChannelUpdate(MENetworkChannelChanged event) {
        List<GridConnectionWrapper> list = new ArrayList<>();
        for (GridConnectionWrapper wrapper : connectionWrappers) {
            if (wrapper.connection.b()
                .equals(event.node)) {
                wrapper.connection.destroy();
            } else {
                list.add(wrapper);
            }
        }
        connectionWrappers.clear();
        connectionWrappers.addAll(list);
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(60, 60, true, true);
    }

    public List<GridConnectionWrapper> getConnectionWrappers() {
        return connectionWrappers;
    }

    private List<Chunk> getChunks(World world) {
        Chunk centerChunk = world.getChunkFromBlockCoords(this.xCoord, this.zCoord);
        List<Chunk> chunks = new LinkedList<>();
        int chunkRadius = (this.radius.getValue() - 1) / 2; // 将区块数量转换为偏移量
        for (int x = -chunkRadius; x <= chunkRadius; x++) {
            for (int z = -chunkRadius; z <= chunkRadius; z++) {
                Chunk chunk = world.getChunkFromChunkCoords(centerChunk.xPosition + x, centerChunk.zPosition + z);

                if (chunk != null && !(chunk instanceof EmptyChunk)) {
                    if (chunk == centerChunk) {
                        // self first
                        chunks.add(0, chunk);
                    } else {
                        chunks.add(chunk);
                    }
                }
            }
        }

        return chunks;
    }

    public void toggleRange() {
        this.radius = Constants.Radius.values()[(this.radius.ordinal() + 1) % Constants.Radius.values().length];
    }

    public Constants.Radius getRadius() {
        return radius;
    }

    private List<TileEntity> getAvailableTileEntity() {
        return this.getAllAvailableTileEntity(
            tile -> tile.getProxy()
                .isReady()
                && !tile.getProxy()
                    .isActive());
    }

    private List<TileEntity> getAllAvailableTileEntity() {
        return this.getAllAvailableTileEntity(tile -> true);
    }

    private List<TileEntity> getAllAvailableTileEntity(Predicate<BaseMetaTileEntity> filter) {
        World world = this.getTile()
            .getWorldObj();
        List<TileEntity> arr = new ArrayList<>();
        for (Chunk c : getChunks(world)) {
            for (Object obj : c.chunkTileEntityMap.values()) {
                if (obj instanceof BaseMetaTileEntity tile && tile.getProxy() != null && filter.test(tile)) {
                    try {
                        arr.add(tile);

                    } catch (Exception ignored) {}
                }
            }
        }
        return arr;
    }

    public void writeAllAvailableTileEntityToNBT(NBTTagCompound data) {
        NBTTagList list = new NBTTagList();
        data.setTag(Constants.TILE_ENTRIES, list);
        for (TileEntity tile : this.getAllAvailableTileEntity()) {
            if (tile instanceof BaseMetaTileEntity bmt) {
                // name color other side
                NBTTagCompound tag = new NBTTagCompound();
                bmt.getLocation()
                    .writeToNBT(tag);
                tag.setString(Constants.NAME, bmt.hasCustomName() ? bmt.getCustomName() : bmt.getLocalName());
                tag.setInteger(
                    Constants.COLOR,
                    bmt.getProxy()
                        .getColor()
                        .ordinal());
                tag.setBoolean(
                    Constants.IS_LINKED,
                    bmt.getProxy()
                        .isActive());
                list.appendTag(tag);
            }
        }
    }

    public boolean doUnLink(DimensionalCoord coord) {
        TileEntity tile = this.getWorldObj()
            .getTileEntity(coord.x, coord.y, coord.z);
        if (!(tile instanceof BaseMetaTileEntity)) return false;
        for (GridConnectionWrapper wrapper : connectionWrappers) {
            if (wrapper.tile.equals(tile)) {
                wrapper.connection.destroy();
                return true;
            }
        }
        return false;
    }

    public boolean doLink(DimensionalCoord coord) {
        TileEntity tile = this.getWorldObj()
            .getTileEntity(coord.x, coord.y, coord.z);
        if (!(tile instanceof BaseMetaTileEntity)) return false;
        for (GridConnectionWrapper wrapper : connectionWrappers) {
            if (wrapper.tile.equals(tile)) {
                return true;
            }
        }
        if (tile instanceof BaseMetaTileEntity bmt && bmt.getProxy()
            .isReady()) {
            bmt.getProxy()
                .getNode()
                .destroy();
            try {
                GridConnectionWrapper con = doLink(this, tile, bmt);
                if (con != null) {
                    connectionWrappers.add(con);
                    return true;
                }
            } catch (Exception ignored) {}
        }
        return false;
    }

    private static GridConnectionWrapper doLink(IGridProxyable self, TileEntity target, IGridProxyable proxy)
        throws FailedConnection {
        if (isSameColor(
            proxy.getProxy()
                .getColor(),
            self.getProxy()
                .getColor())) {
            return new GridConnectionWrapper(
                target,
                AEApi.instance()
                    .createGridConnection(
                        self.getProxy()
                            .getNode(),
                        proxy.getProxy()
                            .getNode()));
        }
        return null;
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall) {
        if (this.getProxy()
            .isActive()) {
            for (TileEntity tile : this.getAvailableTileEntity()) {
                if (this.connectionWrappers.size() < MAX_SIZE) {
                    break;
                }
                if (tile instanceof IGridProxyable proxy) {
                    try {
                        GridConnectionWrapper con = doLink(this, tile, proxy);
                        if (con != null) {
                            connectionWrappers.add(con);
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
        this.scanning = false;
        return TickRateModulation.SLEEP;
    }

    public boolean isScanning() {
        return scanning;
    }

    public int getMaxChannels() {
        return ((GridNode) this.getProxy()
            .getNode()).getMaxChannels();
    }
}
