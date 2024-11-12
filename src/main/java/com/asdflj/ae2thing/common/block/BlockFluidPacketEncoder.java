package com.asdflj.ae2thing.common.block;

import static net.minecraft.client.gui.GuiScreen.isShiftKeyDown;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.common.item.BaseItemBlock;
import com.asdflj.ae2thing.common.tabs.AE2ThingTabs;
import com.asdflj.ae2thing.common.tile.TileFluidPacketEncoder;
import com.asdflj.ae2thing.inventory.InventoryHandler;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.asdflj.ae2thing.loader.IRegister;
import com.asdflj.ae2thing.util.BlockPos;
import com.asdflj.ae2thing.util.NameConst;
import com.glodblock.github.util.RenderUtil;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockFluidPacketEncoder extends BaseTileBlock implements IRegister<BlockFluidPacketEncoder> {

    public BlockFluidPacketEncoder() {
        super(Material.iron);
        this.setBlockName(NameConst.BLOCK_FLUID_PACKET_ENCODER);
        this.setBlockTextureName(AE2Thing.MODID + ":" + NameConst.BLOCK_FLUID_PACKET_ENCODER);
        setFullBlock(true);
        setOpaque(true);
        setTileEntity(TileFluidPacketEncoder.class);
    }

    public void setOpaque(boolean opaque) {
        this.isOpaque = opaque;
    }

    public void setFullBlock(boolean full) {
        this.isFullSize = full;
    }

    @Override
    public BlockFluidPacketEncoder register() {
        GameRegistry.registerBlock(this, BaseItemBlock.class, NameConst.BLOCK_FLUID_PACKET_ENCODER);
        GameRegistry.registerTileEntity(TileFluidPacketEncoder.class, NameConst.BLOCK_FLUID_PACKET_ENCODER);
        setCreativeTab(AE2ThingTabs.INSTANCE);
        return this;
    }

    @Override
    public boolean onActivated(World world, int x, int y, int z, EntityPlayer player, int facing, float hitX,
        float hitY, float hitZ) {
        if (player.isSneaking()) {
            return false;
        }
        TileFluidPacketEncoder tile = getTileEntity(world, x, y, z);
        if (tile != null) {
            if (!world.isRemote) {
                InventoryHandler.openGui(
                    player,
                    world,
                    new BlockPos(x, y, z),
                    ForgeDirection.getOrientation(facing),
                    GuiType.FLUID_PACKET_ENCODER);
            }
            return true;
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack itemStack, final EntityPlayer player, final List<String> toolTip,
        final boolean advancedToolTips) {
        if (isShiftKeyDown()) {
            toolTip.addAll(RenderUtil.listFormattedStringToWidth(I18n.format(NameConst.TT_FLUID_PACKET_ENCODER_DESC)));
        } else {
            toolTip.add(I18n.format(NameConst.TT_SHIFT_FOR_MORE));
        }
    }
}
