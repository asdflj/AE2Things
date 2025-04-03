package com.asdflj.ae2thing.util;

import java.util.Objects;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.util.DimensionalCoord;

public class BlockPos {

    private final int x;
    private final int y;
    private final int z;
    private final World w;

    public BlockPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = null;
    }

    public BlockPos(int x, int y, int z, World w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public BlockPos(@Nonnull TileEntity te) {
        this.x = te.xCoord;
        this.y = te.yCoord;
        this.z = te.zCoord;
        this.w = te.getWorldObj();
    }

    public BlockPos(@Nonnull MovingObjectPosition te, World w) {
        this.x = te.blockX;
        this.y = te.blockY;
        this.z = te.blockZ;
        this.w = w;
    }

    public BlockPos(DimensionalCoord dimensionalCoord) {
        this(dimensionalCoord.x, dimensionalCoord.y, dimensionalCoord.z, dimensionalCoord.getWorld());
    }

    public BlockPos getOffSet(ForgeDirection face) {
        return new BlockPos(this.x + face.offsetX, this.y + face.offsetY, this.z + face.offsetZ, this.w);
    }

    public DimensionalCoord getDimensionalCoord() {
        return new DimensionalCoord(this.w, this.x, this.y, this.z);
    }

    public BlockPos getOffSet(int x, int y, int z) {
        return new BlockPos(this.x + x, this.y + y, this.z + z, this.w);
    }

    public TileEntity getTileEntity() {
        if (w != null) {
            return w.getTileEntity(x, y, z);
        }
        return null;
    }

    public Block getBlock() {
        if (w != null) {
            return w.getBlock(x, y, z);
        }
        return null;
    }

    public ItemStack getPickBlock(MovingObjectPosition target, World world, EntityPlayer player) {
        if (this.getBlock() != null) {
            return this.getBlock()
                .getPickBlock(target, world, this.x, this.y, this.z, player);
        }
        return null;
    }

    public int getBlockPosMetaData() {
        if (w != null) {
            return w.getBlockMetadata(x, y, z);
        }
        throw new IllegalArgumentException("world is null");
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public World getWorld() {
        return w;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BlockPos pos) {
            return pos.x == x && pos.y == y && pos.z == z && Objects.equals(pos.w, w);
        }
        return false;
    }
}
