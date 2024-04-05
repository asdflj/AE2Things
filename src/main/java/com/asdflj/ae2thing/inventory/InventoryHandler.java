package com.asdflj.ae2thing.inventory;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import com.asdflj.ae2thing.util.BlockPos;

import appeng.util.Platform;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class InventoryHandler implements IGuiHandler {

    public static void openGui(EntityPlayer player, World world, BlockPos pos, ForgeDirection face, GuiType guiType) {
        if (Platform.isClient()) {
            return;
        }
        player.openGui(
            AE2Thing.INSTANCE,
            (guiType.ordinal() << 3) | face.ordinal(),
            world,
            pos.getX(),
            pos.getY(),
            pos.getZ());
    }

    @Nullable
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        int faceOrd = id & 0x7;
        if (faceOrd > ForgeDirection.values().length) {
            return null;
        }
        ForgeDirection face = ForgeDirection.getOrientation(faceOrd);
        GuiType type = GuiType.getByOrdinal(id >>> 3);
        return type != null ? type.guiFactory.createServerGui(player, world, x, y, z, face) : null;
    }

    @SideOnly(Side.CLIENT)
    @Nullable
    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        int faceOrd = id & 0x7;
        if (faceOrd > ForgeDirection.values().length) {
            return null;
        }
        ForgeDirection face = ForgeDirection.getOrientation(faceOrd);
        GuiType type = GuiType.getByOrdinal(id >>> 3);
        return type != null ? type.guiFactory.createClientGui(player, world, x, y, z, face) : null;
    }
}
