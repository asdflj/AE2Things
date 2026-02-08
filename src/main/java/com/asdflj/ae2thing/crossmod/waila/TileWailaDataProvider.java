package com.asdflj.ae2thing.crossmod.waila;

import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.google.common.collect.Lists;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;

public class TileWailaDataProvider implements IWailaDataProvider {

    private final List<IWailaDataProvider> providers;

    public TileWailaDataProvider() {
        final IWailaDataProvider wirelessDistributor = new WirelessDistributor();
        this.providers = Lists.newArrayList(wirelessDistributor);
    }

    @Override
    public ItemStack getWailaStack(final IWailaDataAccessor accessor, final IWailaConfigHandler config) {
        return null;
    }

    @Override
    public List<String> getWailaHead(final ItemStack itemStack, final List<String> currentToolTip,
        final IWailaDataAccessor accessor, final IWailaConfigHandler config) {
        for (final IWailaDataProvider provider : this.providers) {
            provider.getWailaHead(itemStack, currentToolTip, accessor, config);
        }

        return currentToolTip;
    }

    @Override
    public List<String> getWailaBody(final ItemStack itemStack, final List<String> currentToolTip,
        final IWailaDataAccessor accessor, final IWailaConfigHandler config) {
        for (final IWailaDataProvider provider : this.providers) {
            provider.getWailaBody(itemStack, currentToolTip, accessor, config);
        }

        return currentToolTip;
    }

    @Override
    public List<String> getWailaTail(final ItemStack itemStack, final List<String> currentToolTip,
        final IWailaDataAccessor accessor, final IWailaConfigHandler config) {
        for (final IWailaDataProvider provider : this.providers) {
            provider.getWailaTail(itemStack, currentToolTip, accessor, config);
        }

        return currentToolTip;
    }

    @Override
    public NBTTagCompound getNBTData(final EntityPlayerMP player, final TileEntity te, final NBTTagCompound tag,
        final World world, final int x, final int y, final int z) {
        for (final IWailaDataProvider provider : this.providers) {
            provider.getNBTData(player, te, tag, world, x, y, z);
        }

        return tag;
    }
}
