package com.asdflj.ae2thing.common.parts;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

import com.asdflj.ae2thing.inventory.InventoryHandler;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.asdflj.ae2thing.util.BlockPos;
import com.glodblock.github.client.textures.FCPartsTexture;
import com.glodblock.github.util.Util;

import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.TypeFilter;
import appeng.api.config.ViewItems;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.implementations.tiles.IViewCellStorage;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridHost;
import appeng.api.networking.events.MENetworkBootingStatusChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.IConfigManager;
import appeng.client.texture.CableBusTextures;
import appeng.me.GridAccessException;
import appeng.parts.AEBasePart;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;

public abstract class THPart extends AEBasePart
    implements IPowerChannelState, ITerminalHost, IConfigManagerHost, IViewCellStorage, IAEAppEngInventory {

    protected static final int POWERED_FLAG = 4;
    protected static final int CHANNEL_FLAG = 16;
    private static final int BOOTING_FLAG = 8;

    private byte spin = 0; // 0-3
    private int clientFlags = 0; // sent as byte.
    private float opacity = -1;

    private final IConfigManager cm = new ConfigManager(this);
    private final AppEngInternalInventory viewCell = new AppEngInternalInventory(this, 5);

    public THPart(final ItemStack is) {
        this(is, false);
    }

    protected THPart(final ItemStack is, final boolean requireChannel) {
        super(is);

        if (requireChannel) {
            this.getProxy()
                .setFlags(GridFlags.REQUIRE_CHANNEL);
            this.getProxy()
                .setIdlePowerUsage(1.0 / 2.0);
        } else {
            this.getProxy()
                .setIdlePowerUsage(1.0 / 16.0); // lights drain a little
        }
        this.cm.registerSetting(Settings.SORT_BY, SortOrder.NAME);
        this.cm.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
        this.cm.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);
        this.cm.registerSetting(Settings.TYPE_FILTER, TypeFilter.ALL);
    }

    @MENetworkEventSubscribe
    public final void bootingRender(final MENetworkBootingStatusChange c) {
        if (!this.isLightSource()) {
            this.getHost()
                .markForUpdate();
        }
    }

    @MENetworkEventSubscribe
    public final void powerRender(final MENetworkPowerStatusChange c) {
        this.getHost()
            .markForUpdate();
    }

    @Override
    public final void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(2, 2, 14, 14, 14, 16);
        bch.addBox(4, 4, 13, 12, 12, 14);
    }

    @Override
    public void onNeighborChanged() {
        this.opacity = -1;
        this.getHost()
            .markForUpdate();
    }

    @Override
    public void getDrops(final List<ItemStack> drops, final boolean wrenched) {
        super.getDrops(drops, wrenched);

        for (final ItemStack is : this.viewCell) {
            if (is != null) {
                drops.add(is);
            }
        }
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.cm;
    }

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey("opacity")) {
            this.opacity = data.getFloat("opacity");
        }
        this.spin = data.getByte("spin");
        this.cm.readFromNBT(data);
        this.viewCell.readFromNBT(data, "viewCell");
    }

    @Override
    public void writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);
        data.setFloat("opacity", this.opacity);
        data.setByte("spin", this.getSpin());
        this.cm.writeToNBT(data);
        this.viewCell.writeToNBT(data, "viewCell");
    }

    public abstract GuiType getGui();

    @Override
    public void writeToStream(final ByteBuf data) throws IOException {
        super.writeToStream(data);
        this.clientFlags = this.getSpin() & 3;

        try {
            if (this.getProxy()
                .getEnergy()
                .isNetworkPowered()) {
                this.clientFlags = this.getClientFlags() | THPart.POWERED_FLAG;
            }

            if (this.getProxy()
                .getPath()
                .isNetworkBooting()) {
                this.clientFlags = this.getClientFlags() | THPart.BOOTING_FLAG;
            }

            if (this.getProxy()
                .getNode()
                .meetsChannelRequirements()) {
                this.clientFlags = this.getClientFlags() | THPart.CHANNEL_FLAG;
            }
        } catch (final GridAccessException ignored) {}

        data.writeByte((byte) this.getClientFlags());
    }

    @Override
    public boolean readFromStream(final ByteBuf data) throws IOException {
        super.readFromStream(data);
        final int oldFlags = this.getClientFlags();
        this.clientFlags = data.readByte();
        this.spin = (byte) (this.getClientFlags() & 3);
        return this.getClientFlags() != oldFlags;
    }

    @Override
    public final int getLightLevel() {
        return this.blockLight(this.isPowered() ? (this.isLightSource() ? 15 : 9) : 0);
    }

    public boolean onPartActivate0(final EntityPlayer player, final Vec3 pos) {
        final TileEntity te = this.getTile();

        if (!player.isSneaking()
            && Platform.isWrench(player, player.inventory.getCurrentItem(), te.xCoord, te.yCoord, te.zCoord)) {
            if (Platform.isServer()) {
                if (this.getSpin() > 3) {
                    this.spin = 0;
                }

                switch (this.getSpin()) {
                    case 0 -> this.spin = 1;
                    case 1 -> this.spin = 3;
                    case 2 -> this.spin = 0;
                    case 3 -> this.spin = 2;
                }

                this.getHost()
                    .markForUpdate();
                this.saveChanges();
            }
            return true;
        } else {
            return super.onPartActivate(player, pos);
        }
    }

    @Override
    public boolean onPartActivate(final EntityPlayer player, final Vec3 pos) {
        TileEntity te = this.getTile();
        BlockPos tePos = new BlockPos(te);
        if (Platform.isWrench(player, player.inventory.getCurrentItem(), tePos.getX(), tePos.getY(), tePos.getZ())) {
            return super.onPartActivate(player, pos);
        }
        if (Platform.isServer()) {
            if (Util.hasPermission(player, SecurityPermissions.INJECT, (IGridHost) this)
                || Util.hasPermission(player, SecurityPermissions.EXTRACT, (IGridHost) this)) {
                InventoryHandler.openGui(player, te.getWorldObj(), tePos, Objects.requireNonNull(getSide()), getGui());
            } else {
                player.addChatComponentMessage(new ChatComponentText("You don't have permission to view."));
            }
        }
        return true;
    }

    @Override
    public final void onPlacement(final EntityPlayer player, final ItemStack held, final ForgeDirection side) {
        super.onPlacement(player, held, side);

        final byte rotation = (byte) (MathHelper.floor_double((player.rotationYaw * 4F) / 360F + 2.5D) & 3);
        if (side == ForgeDirection.UP) {
            this.spin = rotation;
        } else if (side == ForgeDirection.DOWN) {
            this.spin = rotation;
        }
    }

    private int blockLight(final int emit) {
        if (this.opacity < 0) {
            final TileEntity te = this.getTile();
            this.opacity = 255 - te.getWorldObj()
                .getBlockLightOpacity(
                    te.xCoord + this.getSide().offsetX,
                    te.yCoord + this.getSide().offsetY,
                    te.zCoord + this.getSide().offsetZ);
        }
        return (int) (emit * (this.opacity / 255.0f));
    }

    @Override
    public final boolean isPowered() {
        try {
            if (Platform.isServer()) {
                return this.getProxy()
                    .getEnergy()
                    .isNetworkPowered();
            } else {
                return ((this.getClientFlags() & THPart.POWERED_FLAG) == THPart.POWERED_FLAG);
            }
        } catch (final GridAccessException e) {
            return false;
        }
    }

    @Override
    public IMEMonitor<IAEItemStack> getItemInventory() {
        try {
            return this.getProxy()
                .getStorage()
                .getItemInventory();
        } catch (final GridAccessException ignored) {}
        return null;
    }

    @Override
    public IMEMonitor<IAEFluidStack> getFluidInventory() {
        try {
            return this.getProxy()
                .getStorage()
                .getFluidInventory();
        } catch (final GridAccessException ignored) {}
        return null;
    }

    @Override
    public void updateSetting(final IConfigManager manager, final Enum settingName, final Enum newValue) {}

    @Override
    public IInventory getViewCellStorage() {
        return this.viewCell;
    }

    @Override
    public void onChangeInventory(final IInventory inv, final int slot, final InvOperation mc,
        final ItemStack removedStack, final ItemStack newStack) {
        this.getHost()
            .markForSave();
    }

    @Override
    public final boolean isActive() {
        if (!this.isLightSource()) {
            return ((this.getClientFlags() & (THPart.CHANNEL_FLAG | THPart.POWERED_FLAG))
                == (THPart.CHANNEL_FLAG | THPart.POWERED_FLAG));
        } else {
            return this.isPowered();
        }
    }

    public final int getClientFlags() {
        return this.clientFlags;
    }

    public final byte getSpin() {
        return this.spin;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderInventory(final IPartRenderHelper rh, final RenderBlocks renderer) {
        rh.setBounds(2, 2, 14, 14, 14, 16);

        final IIcon sideTexture = CableBusTextures.PartMonitorSides.getIcon();
        final IIcon backTexture = CableBusTextures.PartMonitorBack.getIcon();

        rh.setTexture(
            sideTexture,
            sideTexture,
            backTexture,
            FCPartsTexture.PartTerminalBroad.getIcon(),
            sideTexture,
            sideTexture);
        rh.renderInventoryBox(renderer);

        rh.setInvColor(this.getColor().whiteVariant);
        rh.renderInventoryFace(
            this.getFrontBright()
                .getIcon(),
            ForgeDirection.SOUTH,
            renderer);

        rh.setInvColor(this.getColor().mediumVariant);
        rh.renderInventoryFace(
            this.getFrontDark()
                .getIcon(),
            ForgeDirection.SOUTH,
            renderer);

        rh.setInvColor(this.getColor().blackVariant);
        rh.renderInventoryFace(
            this.getFrontColored()
                .getIcon(),
            ForgeDirection.SOUTH,
            renderer);

        rh.setBounds(4, 4, 13, 12, 12, 14);
        rh.renderInventoryBox(renderer);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderStatic(final int x, final int y, final int z, final IPartRenderHelper rh,
        final RenderBlocks renderer) {
        this.setRenderCache(rh.useSimplifiedRendering(x, y, z, this, this.getRenderCache()));

        final IIcon sideTexture = CableBusTextures.PartMonitorSides.getIcon();
        final IIcon backTexture = CableBusTextures.PartMonitorBack.getIcon();

        rh.setTexture(
            sideTexture,
            sideTexture,
            backTexture,
            FCPartsTexture.PartTerminalBroad.getIcon(),
            sideTexture,
            sideTexture);

        rh.setBounds(2, 2, 14, 14, 14, 16);
        rh.renderBlock(x, y, z, renderer);

        if (this.getLightLevel() > 0) {
            final int l = 13;
            Tessellator.instance.setBrightness(l << 20 | l << 4);
        }

        renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = this
            .getSpin();

        Tessellator.instance.setColorOpaque_I(this.getColor().whiteVariant);
        rh.renderFace(
            x,
            y,
            z,
            this.getFrontBright()
                .getIcon(),
            ForgeDirection.SOUTH,
            renderer);

        Tessellator.instance.setColorOpaque_I(this.getColor().mediumVariant);
        rh.renderFace(
            x,
            y,
            z,
            this.getFrontDark()
                .getIcon(),
            ForgeDirection.SOUTH,
            renderer);

        Tessellator.instance.setColorOpaque_I(this.getColor().blackVariant);
        rh.renderFace(
            x,
            y,
            z,
            this.getFrontColored()
                .getIcon(),
            ForgeDirection.SOUTH,
            renderer);

        renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;

        final IIcon sideStatusTexture = CableBusTextures.PartMonitorSidesStatus.getIcon();

        rh.setTexture(
            sideStatusTexture,
            sideStatusTexture,
            backTexture,
            this.getItemStack()
                .getIconIndex(),
            sideStatusTexture,
            sideStatusTexture);

        rh.setBounds(4, 4, 13, 12, 12, 14);
        rh.renderBlock(x, y, z, renderer);

        final boolean hasChan = (this.getClientFlags() & (THPart.POWERED_FLAG | THPart.CHANNEL_FLAG))
            == (THPart.POWERED_FLAG | THPart.CHANNEL_FLAG);
        final boolean hasPower = (this.getClientFlags() & THPart.POWERED_FLAG) == THPart.POWERED_FLAG;

        if (hasChan) {
            final int l = 14;
            Tessellator.instance.setBrightness(l << 20 | l << 4);
            Tessellator.instance.setColorOpaque_I(this.getColor().blackVariant);
        } else if (hasPower) {
            final int l = 9;
            Tessellator.instance.setBrightness(l << 20 | l << 4);
            Tessellator.instance.setColorOpaque_I(this.getColor().whiteVariant);
        } else {
            Tessellator.instance.setBrightness(0);
            Tessellator.instance.setColorOpaque_I(0x000000);
        }

        final IIcon sideStatusLightTexture = CableBusTextures.PartMonitorSidesStatusLights.getIcon();

        rh.renderFace(x, y, z, sideStatusLightTexture, ForgeDirection.EAST, renderer);
        rh.renderFace(x, y, z, sideStatusLightTexture, ForgeDirection.WEST, renderer);
        rh.renderFace(x, y, z, sideStatusLightTexture, ForgeDirection.UP, renderer);
        rh.renderFace(x, y, z, sideStatusLightTexture, ForgeDirection.DOWN, renderer);
    }

    /**
     * The texture used for the bright front layer.
     * <p>
     * The final texture can overlap any of the texture in no particular order.
     */
    public abstract FCPartsTexture getFrontBright();

    /**
     * The texture used for the colored (medium) front layer.
     * <p>
     * The final texture can overlap any of the texture in no particular order.
     */
    public abstract FCPartsTexture getFrontColored();

    /**
     * The texture used for the dark front layer.
     * <p>
     * The final texture can overlap any of the texture in no particular order.
     */
    public abstract FCPartsTexture getFrontDark();

    /**
     * Should the part emit light. This actually only affects the light level, light source use a level of 15 and
     * non-light source 9.
     */
    public abstract boolean isLightSource();
}
