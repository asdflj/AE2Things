package com.asdflj.ae2thing.api;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.asdflj.ae2thing.util.Ae2Reflect;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import appeng.api.config.CraftingMode;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCallback;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.security.PlayerSource;
import appeng.api.parts.IPart;
import appeng.api.parts.PartItemStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.DimensionalCoord;
import appeng.core.AELog;
import appeng.crafting.v2.CraftingJobV2;
import appeng.helpers.ICustomNameObject;
import appeng.me.Grid;
import appeng.me.GridStorage;
import appeng.me.helpers.IGridProxyable;
import appeng.parts.AEBasePart;
import appeng.util.Platform;

public class CraftingDebugHelper implements ICraftingCallback {

    private static final HashMap<Long, LimitedSizeLinkedList<CraftingInfo>> history = new HashMap<>();
    private final CraftingInfo info;
    private final ICraftingCallback callback;

    public static class CraftingInfo {

        @Expose
        public String name = "";
        @Expose
        public DimensionalCoord pos;
        @Expose
        public long startTime = 0;
        @Expose
        public long endTime = 0;
        @Expose
        public final String itemName;
        public final long id;
        public final CraftingMode mode;
        @Expose
        public boolean isPlayer;
        @Expose
        public long requestSize;
        @Expose
        public ForgeDirection direction = ForgeDirection.UNKNOWN;
        @Expose
        public String errorMessage = "";
        @Expose
        @SerializedName("simulationState")
        public Constants.State state = Constants.State.RUNNING;
        @Expose
        @SerializedName("missingItems")
        public boolean simulation = false;

        private String getName(IActionHost via, String name) {
            if (via instanceof ICustomNameObject o) {
                return o.hasCustomName() ? o.getCustomName() : name;
            }
            return name;
        }

        public String getErrorMessage() {
            return this.errorMessage;
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
                    this.pos = part.getLocation();
                    this.name = getName(
                        ms.via,
                        part.getItemStack()
                            .getDisplayName());
                    this.direction = part.getSide();
                } else if (ms.via instanceof IPart part) {
                    if (part instanceof IGridProxyable p) {
                        this.pos = p.getLocation();
                    } else {
                        this.pos = null;
                    }
                    this.name = getName(ms.via, Platform.getItemDisplayName(part.getItemStack(PartItemStack.Pick)));
                } else if (ms.via instanceof TileEntity te) {
                    this.pos = new DimensionalCoord(te);
                    this.name = getName(
                        ms.via,
                        te.getBlockType()
                            .getLocalizedName());
                } else {
                    AELog.error("Unknown action source");
                    this.pos = null;
                }
            } else {
                AELog.error("Unknown action source");
                this.pos = null;
            }
            this.id = id;
            this.mode = mode;
            this.itemName = Platform.getItemDisplayName(item);
            this.requestSize = item.getStackSize();
        }

        public CraftingInfo(long id, String name, long startTime, long endTime, String itemName, byte mode,
            long requestSize, byte direction, boolean isPlayer, DimensionalCoord pos, String errorMessage,
            Constants.State state, boolean simulation) {
            this.id = id;
            this.name = name;
            this.startTime = startTime;
            this.endTime = endTime;
            this.itemName = itemName;
            this.requestSize = requestSize;
            this.mode = CraftingMode.values()[mode];
            this.direction = ForgeDirection.getOrientation(direction);
            this.isPlayer = isPlayer;
            this.pos = pos;
            this.errorMessage = errorMessage;
            this.state = state;
            this.simulation = simulation;
        }

        public boolean isPart() {
            return this.direction != ForgeDirection.UNKNOWN;
        }

        public void writeToNBT(NBTTagCompound tag) {
            tag.setLong("nid", this.id);
            tag.setString("name", this.name);
            tag.setLong("startTime", this.startTime);
            tag.setLong("endTime", this.endTime);
            tag.setString("itemName", this.itemName);
            tag.setByte("mode", (byte) this.mode.ordinal());
            tag.setLong("requestSize", this.requestSize);
            tag.setByte("direction", (byte) this.direction.ordinal());
            tag.setBoolean("isPlayer", this.isPlayer);
            tag.setString("errorMsg", this.errorMessage);
            tag.setByte("state", (byte) this.state.ordinal());
            tag.setBoolean("simulation", this.simulation);
            if (!this.isPlayer && this.pos != null) this.pos.writeToNBT(tag);
        }

        public static void writeToNBTList(List<CraftingInfo> infos, NBTTagCompound tag, long networkID) {
            NBTTagList list = new NBTTagList();
            for (CraftingInfo info : infos) {
                NBTTagCompound data = new NBTTagCompound();
                info.writeToNBT(data);
                list.appendTag(data);
            }
            tag.setTag("infos", list);
            tag.setInteger("size", infos.size());
            tag.setLong("networkID", networkID);
        }

        public static LimitedSizeLinkedList<CraftingInfo> readFromNBTList(NBTTagCompound tag) {
            LimitedSizeLinkedList<CraftingInfo> infos = new LimitedSizeLinkedList<>();
            NBTTagList list = tag.getTagList("infos", 10);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound data = list.getCompoundTagAt(i);
                infos.add(CraftingInfo.readFromNBT(data));
            }
            return infos;
        }

        public static CraftingInfo readFromNBT(NBTTagCompound tag) {
            long id = tag.getLong("nid");
            String name = tag.getString("name");
            long startTime = tag.getLong("startTime");
            long endTime = tag.getLong("endTime");
            String itemName = tag.getString("itemName");
            byte mode = tag.getByte("mode");
            long requestSize = tag.getLong("requestSize");
            byte direction = tag.getByte("direction");
            boolean isPlayer = tag.getBoolean("isPlayer");
            String msg = tag.getString("errorMsg");
            boolean simulation = tag.getBoolean("simulation");
            Constants.State state = Constants.State.values()[tag.getByte("state")];
            DimensionalCoord pos = null;
            if (!isPlayer && tag.hasKey("dim")) {
                pos = DimensionalCoord.readFromNBT(tag);
            }
            return new CraftingInfo(
                id,
                name,
                startTime,
                endTime,
                itemName,
                mode,
                requestSize,
                direction,
                isPlayer,
                pos,
                msg,
                state,
                simulation);
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

        public void setErrorMessage(String message) {
            this.errorMessage = message;
        }

        public void setState(Constants.State state) {
            this.state = state;
        }

        public Constants.State getState() {
            return this.state;
        }

        public void setSimulation(boolean simulation) {
            this.simulation = simulation;
        }

        public boolean isSimulation() {
            return this.simulation;
        }
    }

    public CraftingDebugHelper(final World world, final IGrid meGrid, final BaseActionSource actionSource,
        final IAEItemStack what, final CraftingMode craftingMode, ICraftingCallback callback) {
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
        this.callback = callback;
    }

    public static void craftingHelper(final CraftingJobV2 jobV2, final World world, final IGrid meGrid,
        final BaseActionSource actionSource, final IAEItemStack what, final CraftingMode craftingMode,
        final ICraftingCallback callback) {
        Ae2Reflect
            .setCallback(jobV2, new CraftingDebugHelper(world, meGrid, actionSource, what, craftingMode, callback));
    }

    public static void remove(GridStorage gridStorage) {
        history.remove(gridStorage.getID());
    }

    @Override
    public void calculationComplete(ICraftingJob job) {
        this.info.setEndTime(System.currentTimeMillis());
        info.setSimulation(job.isSimulation());
        if (job instanceof CraftingJobV2 v2) {
            this.info.setErrorMessage(v2.getErrorMessage());
            if (v2.isCancelled()) {
                this.info.setState(Constants.State.CANCELLED);
            } else if (v2.isDone()) {
                this.info.setState(Constants.State.FINISHED);
            } else {
                this.info.setState(Constants.State.RUNNING);
            }
        }
        if (this.callback != null) {
            this.callback.calculationComplete(job);
        }
    }

    public static HashMap<Long, LimitedSizeLinkedList<CraftingInfo>> getHistory() {
        return history;
    }

    public static CraftingDebugCardObject getObject(ItemStack itemStack) {
        return new CraftingDebugCardObject(itemStack);
    }

    public static Gson getGson() {
        return new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .setPrettyPrinting()
            .registerTypeAdapter(ForgeDirection.class, new ForgeDirectionSerializer())
            .registerTypeAdapter(DimensionalCoord.class, new DimensionalCoordSerializer())
            .registerTypeAdapter(Constants.State.class, new StateSerializer())
            .create();
    }

    private static class StateSerializer implements JsonSerializer<Constants.State> {

        public JsonElement serialize(Constants.State src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.name());
        }
    }

    private static class ForgeDirectionSerializer implements JsonSerializer<ForgeDirection> {

        public JsonElement serialize(ForgeDirection src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.name());
        }
    }

    private static class DimensionalCoordSerializer implements JsonSerializer<DimensionalCoord> {

        @Override
        public JsonElement serialize(DimensionalCoord src, Type typeOfSrc, JsonSerializationContext context) {
            if (src == null) return null;
            JsonObject o = new JsonObject();
            o.addProperty("X", src.x);
            o.addProperty("Y", src.y);
            o.addProperty("Z", src.z);
            o.addProperty("Dim", src.getDimension());
            return o;
        }
    }
}
