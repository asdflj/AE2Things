package com.asdflj.ae2thing.coremod.hooker;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import com.asdflj.ae2thing.common.Config;
import com.asdflj.ae2thing.common.parts.PartThaumatoriumInterface;
import com.asdflj.ae2thing.common.tile.TileExIOPort;
import com.asdflj.ae2thing.common.tile.TileInfusionInterface;
import com.asdflj.ae2thing.inventory.EssentiaInventoryAdaptor;
import com.asdflj.ae2thing.inventory.ThaumatoriumInventoryAdapter;
import com.asdflj.ae2thing.util.ModAndClassUtil;

import appeng.api.parts.IPart;
import appeng.tile.storage.TileIOPort;
import appeng.util.InventoryAdaptor;
import thaumcraft.common.tiles.TileThaumatorium;

public class CoreModHooks {

    public static InventoryAdaptor getAdaptor(TileEntity tile, ForgeDirection face) {
        if (tile == null) return null;
        TileEntity inter = tile.getWorldObj()
            .getTileEntity(tile.xCoord + face.offsetX, tile.yCoord + face.offsetY, tile.zCoord + face.offsetZ);
        if (ModAndClassUtil.THE) {
            if (inter instanceof TileInfusionInterface) {
                return EssentiaInventoryAdaptor.getAdaptor(tile, face);
            } else if (com.glodblock.github.util.Util
                .getPart(inter, face.getOpposite()) instanceof PartThaumatoriumInterface) {
                    return ThaumatoriumInventoryAdapter.getAdaptor(tile, face);
                }
        }
        return InventoryAdaptor.getAdaptor(tile, face);
    }

    public static void getConnectableTile(TileThaumatorium tile, int y, ForgeDirection face) {
        TileEntity cable = tile.getWorldObj()
            .getTileEntity(tile.xCoord + face.offsetX, tile.yCoord + face.offsetY + y, tile.zCoord + face.offsetZ);
        if (cable == null) return;
        IPart part = com.glodblock.github.util.Util.getPart(cable, face.getOpposite());
        if (ModAndClassUtil.THE && part instanceof PartThaumatoriumInterface pti) {
            int ess = pti.takeEssentia(tile.currentSuction, 1, face);
            if (ess > 0) {
                tile.addToContainer(tile.currentSuction, ess);
            }
        }
    }

    public static long getItemsToMove(TileIOPort ioPort, long base) {
        if (ioPort instanceof TileExIOPort) {
            return Config.exIOPortTransferContentsRate * base;
        } else {
            return base;
        }

    }

}
