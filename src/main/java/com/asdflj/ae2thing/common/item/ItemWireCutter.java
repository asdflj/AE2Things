package com.asdflj.ae2thing.common.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.common.tabs.AE2ThingTabs;
import com.asdflj.ae2thing.util.NameConst;

import appeng.api.networking.events.MENetworkChannelChanged;
import appeng.util.Platform;
import cpw.mods.fml.common.registry.GameRegistry;
import gregtech.api.metatileentity.BaseMetaTileEntity;

public class ItemWireCutter extends BaseItem {

    public ItemWireCutter() {
        super();
        this.setMaxStackSize(1);
        this.setUnlocalizedName(NameConst.ITEM_WIRE_CUTTER);
        this.setTextureName(
            AE2Thing.resource(NameConst.ITEM_WIRE_CUTTER)
                .toString());
    }

    @Override
    public ItemWireCutter register() {
        GameRegistry.registerItem(this, NameConst.ITEM_WIRE_CUTTER, AE2Thing.MODID);
        setCreativeTab(AE2ThingTabs.INSTANCE);
        return this;
    }

    @Override
    public boolean onItemUseFirst(ItemStack itemstack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ) {
        if (Platform.isServer()) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof BaseMetaTileEntity proxy) {
                try {
                    proxy.getProxy()
                        .getGrid()
                        .postEvent(
                            new MENetworkChannelChanged(
                                proxy.getProxy()
                                    .getNode()));
                    proxy.getProxy()
                        .getNode()
                        .destroy();
                } catch (Exception ignored) {}

            }
        }
        return super.onItemUseFirst(itemstack, player, world, x, y, z, side, hitX, hitY, hitZ);
    }
}
