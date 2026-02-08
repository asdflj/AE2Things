package com.asdflj.ae2thing.crossmod.waila;

import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.asdflj.ae2thing.common.tile.TileWirelessDistributor;

import appeng.core.localization.WailaText;
import appeng.integration.modules.waila.BaseWailaDataProvider;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;

public class WirelessDistributor extends BaseWailaDataProvider {

    private static final String ID_USED_CHANNELS = "usedChannels";
    private static final String ID_MAX_CHANNELS = "maxChannels";

    @Override
    public List<String> getWailaBody(final ItemStack itemStack, final List<String> currentToolTip,
        final IWailaDataAccessor accessor, final IWailaConfigHandler config) {
        final TileEntity te = accessor.getTileEntity();
        if (te instanceof TileWirelessDistributor) {
            NBTTagCompound data = accessor.getNBTData();
            currentToolTip.add(
                String.format(
                    WailaText.Channels.getLocal(),
                    data.getInteger(ID_USED_CHANNELS),
                    data.getInteger(ID_MAX_CHANNELS)));
        }
        return currentToolTip;
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, int x,
        int y, int z) {
        if (te instanceof TileWirelessDistributor tileWirelessDistributor) {
            tag.setInteger(ID_USED_CHANNELS, tileWirelessDistributor.getUsedChannels());
            tag.setInteger(ID_MAX_CHANNELS, tileWirelessDistributor.getMaxChannels());
        }

        return tag;
    }
}
