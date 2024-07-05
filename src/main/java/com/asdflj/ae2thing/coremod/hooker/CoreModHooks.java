package com.asdflj.ae2thing.coremod.hooker;

import java.lang.reflect.Field;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.util.ForgeDirection;

import com.asdflj.ae2thing.util.NameConst;
import com.asdflj.ae2thing.util.Util;

import appeng.api.networking.IGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.util.ReadableNumberConverter;

public class CoreModHooks {

    public static void craftingComplete(CraftingCPUCluster cpuCluster) {
        try {
            if (cpuCluster.isBusy()) return;
            Field f;
            f = cpuCluster.getClass()
                .getField("player");
            EntityPlayer player = (EntityPlayer) f.get(cpuCluster);

            if (player != null && cpuCluster.getTiles()
                .hasNext()) {
                IGrid host = cpuCluster.getTiles()
                    .next()
                    .getGridNode(ForgeDirection.UNKNOWN)
                    .getGrid();

                IGrid wHost = Util.getWirelessGrid(player);
                if (host != null && host.equals(wHost)) {
                    f = cpuCluster.getClass()
                        .getField("requestItem");
                    IAEItemStack is = (IAEItemStack) f.get(cpuCluster);
                    if (is != null) {
                        player.addChatMessage(
                            new ChatComponentText(
                                I18n.format(
                                    NameConst.TT_CRAFTING_COMPLETE,
                                    Util.getDisplayName(is),
                                    ReadableNumberConverter.INSTANCE.toWideReadableForm(is.getStackSize()))));
                    }
                }
            }
        } catch (Exception ignored) {

        }
    }

    public static void craftingAddActionSource(CraftingCPUCluster cpuCluster, BaseActionSource src) {
        try {
            Field f;
            f = cpuCluster.getClass()
                .getField("player");
            if (src instanceof PlayerSource) {
                f.set(cpuCluster, ((PlayerSource) src).player);
            } else {
                f.set(cpuCluster, null);
            }
            f = cpuCluster.getClass()
                .getField("requestItem");
            IAEItemStack is = cpuCluster.getFinalOutput();
            if (is != null) {
                f.set(cpuCluster, is.copy());
            }

        } catch (Exception ignored) {

        }
    }
}
