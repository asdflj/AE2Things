package com.asdflj.ae2thing.crossmod.waila;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import com.asdflj.ae2thing.common.tile.TileWirelessDistributor;
import com.asdflj.ae2thing.util.NameConst;

import appeng.core.localization.WailaText;
import appeng.integration.modules.waila.BaseWailaDataProvider;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;

public class WirelessDistributor extends BaseWailaDataProvider {

    private static final String ID_USED_CHANNELS = "usedChannels";
    private static final String ID_MAX_CHANNELS = "maxChannels";
    private static final String SCANNING = "scanning";

    @Override
    public List<String> getWailaBody(final ItemStack itemStack, final List<String> currentToolTip,
        final IWailaDataAccessor accessor, final IWailaConfigHandler config) {
        final TileEntity te = accessor.getTileEntity();
        if (te instanceof TileWirelessDistributor tile) {
            NBTTagCompound data = accessor.getNBTData();
            currentToolTip.add(
                String.format(
                    WailaText.Channels.getLocal(),
                    data.getInteger(ID_USED_CHANNELS),
                    data.getInteger(ID_MAX_CHANNELS)));
            currentToolTip.add(
                tile.getColor()
                    .toString());
            if (data.getBoolean(SCANNING)) {
                currentToolTip.add(EnumChatFormatting.GREEN + I18n.format(NameConst.MESSAGE_SCANNING));
            }

        }
        return currentToolTip;
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, int x,
        int y, int z) {
        if (te instanceof TileWirelessDistributor tileWirelessDistributor) {
            tileWirelessDistributor.updateUsedChannels();
            tag.setInteger(ID_USED_CHANNELS, tileWirelessDistributor.getUsedChannels());
            tag.setInteger(ID_MAX_CHANNELS, tileWirelessDistributor.getMaxChannels());
            tag.setBoolean(SCANNING, tileWirelessDistributor.isScanning());
        }

        return tag;
    }
}
