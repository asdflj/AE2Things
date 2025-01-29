package com.asdflj.ae2thing.api;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.asdflj.ae2thing.common.Config;
import com.asdflj.ae2thing.util.Ae2Reflect;
import com.asdflj.ae2thing.util.BlockPos;

import appeng.api.config.CraftingMode;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCallback;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.crafting.v2.CraftingJobV2;
import appeng.helpers.ICustomNameObject;
import appeng.me.Grid;
import appeng.me.GridStorage;
import appeng.parts.AEBasePart;
import appeng.util.Platform;

public class CraftingDebugHelper implements ICraftingCallback {

    private static final HashMap<Long, LimitedSizeLinkedList<CraftingInfo>> history = new HashMap<>();
    private static final int LIMIT = Config.craftingHistorySize;
    private final CraftingInfo info;

    public static class LimitedSizeLinkedList<E> extends LinkedList<E> {

        private final int limit;

        public LimitedSizeLinkedList() {
            super();
            this.limit = LIMIT;
        }

        @Override
        public boolean add(E e) {
            if (size() >= limit) {
                removeFirst();
            }
            return super.add(e);
        }

        @Override
        public void push(E e) {
            if (size() >= limit) {
                removeLast();
            }
            super.push(e);
        }
    }

    public static class CraftingInfo {

        public String name = "";
        public BlockPos pos;
        public long startTime = 0;
        public long endTime = 0;
        public final String itemName;
        public final long id;
        public final CraftingMode mode;
        public boolean isPlayer;
        public long requestSize;
        public ForgeDirection direction;

        private String getName(IActionHost via, String name) {
            if (via instanceof ICustomNameObject o) {
                return o.hasCustomName() ? o.getCustomName() : name;
            }
            return name;
        }

        public String getName() {
            return this.name;
        }

        public CraftingInfo(BaseActionSource actionSource, IAEItemStack item, long id, CraftingMode mode) {
            if (actionSource instanceof PlayerSource ps) {
                this.name = ps.player.getDisplayName();
                this.isPlayer = true;
            } else if (actionSource instanceof MachineSource ms) {
                if (ms.via instanceof AEBasePart part) {
                    this.pos = new BlockPos(part.getLocation());
                    this.name = getName(
                        ms.via,
                        part.getItemStack()
                            .getDisplayName());
                    this.direction = part.getSide();
                } else if (ms.via instanceof TileEntity te) {
                    this.pos = new BlockPos(te);
                    this.name = getName(
                        ms.via,
                        te.getBlockType()
                            .getLocalizedName());
                } else {
                    throw new RuntimeException("Unknown action source");
                }
            } else {
                throw new RuntimeException("Unknown action source");
            }
            this.id = id;
            this.mode = mode;
            this.itemName = Platform.getItemDisplayName(item);
            this.requestSize = item.getStackSize();
        }

        public boolean isPart() {
            return this.direction != null;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public boolean isFinish() {
            return this.endTime != 0;
        }

        public void setEndTime(long endTime) {
            this.endTime = endTime;
        }

        public String getNetworkID() {
            return Long.toString(id);
        }

        public String getFormatStartTime() {
            Date date = new Date(this.startTime);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.format(date);
        }

        public String getUsageTime() {
            return String.format("%s ms", this.endTime - this.startTime);
        }

    }

    public CraftingDebugHelper(final World world, final IGrid meGrid, final BaseActionSource actionSource,
        final IAEItemStack what, final CraftingMode craftingMode) {
        long id = AE2ThingAPI.instance()
            .getStorageMyID((Grid) meGrid);
        history.putIfAbsent(id, new LimitedSizeLinkedList<>());
        this.info = new CraftingInfo(
            actionSource,
            what,
            AE2ThingAPI.instance()
                .getStorageMyID((Grid) meGrid),
            craftingMode);
        this.info.setStartTime(System.currentTimeMillis());
        history.get(id)
            .add(info);
    }

    public static void craftingHelper(final CraftingJobV2 jobV2, final World world, final IGrid meGrid,
        final BaseActionSource actionSource, final IAEItemStack what, final CraftingMode craftingMode,
        final ICraftingCallback callback) {
        if (callback == null) {
            Ae2Reflect.setCallback(jobV2, new CraftingDebugHelper(world, meGrid, actionSource, what, craftingMode));
        }
    }

    public static void remove(GridStorage gridStorage) {
        history.remove(gridStorage.getID());
    }

    @Override
    public void calculationComplete(ICraftingJob job) {
        this.info.setEndTime(System.currentTimeMillis());
    }

    public static HashMap<Long, LimitedSizeLinkedList<CraftingInfo>> getHistory() {
        return history;
    }

    public static CraftingDebugCardObject getObject(ItemStack itemStack) {
        return new CraftingDebugCardObject(itemStack);
    }
}
